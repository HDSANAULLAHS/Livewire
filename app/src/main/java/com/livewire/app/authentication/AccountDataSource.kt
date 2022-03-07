package com.livewire.app.authentication

import android.net.Uri
import android.util.Log
import com.livewire.app.R
import com.livewire.app.authentication.ui.api.DataCenterApi
import com.livewire.app.authentication.ui.api.HarleyAccountApi
import com.livewire.app.authentication.ui.api.UserAccountApi
import com.livewire.app.gigya.GigyaApi
import com.livewire.app.locale.LocaleManager
import com.livewire.app.model.*
import com.livewire.app.session.SessionTokenStore
import com.livewire.app.store.*

// TODO this class is UserAuthentication, Session.)

open class AccountDataSource(val api: UserAccountApi,
                             val harley: HarleyAccountApi,
                             val gigya: GigyaManager,
                             val gigyaApi: GigyaApi,
                             val dataCenters: DataCenterApi,
                             val tokens: SessionTokenStore,
                             val user: UserViewModel,
                             val local : LocaleManager,
                             val preferences : appPreferences, ) : BaseRemoteDataSource() {
    companion object {
        val TAG = "UserAccountDataSource"
    }

    private var lastProfileRefresh = 0L

    fun createAccount(user: User, password: String, success: () -> Unit, failure: (Int) -> Unit) {
        // Before creating account, must ensure that a full account does not exist in any datacenter
        // It is OK to continue if account exists but is a lite account
        getDataCenterForEmail(user.email, {
            if (it.hasFullAccount) {
                // Account exists; failed
                failure(R.string.account_exists)
            } else {
                // Its a lite account, continue with this datacenter
                createAccount(it.dc, user, password, success, failure)
            }
        }, {
            // No account exists, continue with closest datacenter
            createAccount(null, user, password, success, failure)
        })
    }

    private fun createAccount(dataCenter: String?, user: User, password: String, success: () -> Unit, failure: (Int) -> Unit) {
        val dc = dataCenter ?: ""

        if (dc.isNotBlank()) {
            // If already have a datacenter use that and continue
            gigya.initialize(dc)

            continueCreateAccount(user, password, success, failure)
        } else {
            // Use the closest datacenter
            configureGigyaForClosestDataCenter({
                continueCreateAccount(user, password, success, failure)
            }, { failure(GigyaApi.Method.CREATE_ACCOUNT.genericError) })
        }
    }

    private fun continueCreateAccount(user: User, password: String, success: () -> Unit, failure: (Int) -> Unit) {
        gigyaApi.createAccount(user, password, local.getCurrentLanguage().gigyaLocaleCode, {
            continueAuthentication(it, success, {
                failure(GigyaApi.Method.CREATE_ACCOUNT.genericError) })
        }, failure)
    }

    fun login(email: String, password: String, success: () -> Unit, failure: (Int) -> Unit, changePasswordRequired: (String) -> Unit) {
        // Helper to wrap login and continue
        fun tryLogin(failed: (Int) -> Unit = failure) {
            gigyaApi.login(email, password, { continueAuthentication(it, success, failure) }, failed, changePasswordRequired)
        }

        // Need to configure gigya for the correct datacenter for this email address before login.
        // The call to get the datacenter for an account is slow, so first just attempt to login
        // to the closest data center. If that doesnt work, then make the slow call and retry login.

        // Try closest
        configureGigyaForClosestDataCenter({ dc ->
            tryLogin {
                // That didnt work, try datacenter lookup
                configureGigyaForAccountDataCenter(email, dc, {
                    tryLogin()
                }, {
                    failure(GigyaApi.ERROR_MESSAGES[GigyaApi.ERROR_INVALID_LOGIN] ?: R.string.invalid_user_password)
                })
            }
        }, { failure(GigyaApi.Method.LOGIN.genericError) })
    }

    fun checkPassword(email: String, password: String, success: () -> Unit, failure: (Int) -> Unit) {
        // Helper to wrap login and continue
        fun tryLogin(failed: (Int) -> Unit = failure) {
            gigyaApi.checkPassword(email, password, { success() }, { failed(it) })
        }

        // Need to configure gigya for the correct datacenter for this email address before login.
        // The call to get the datacenter for an account is slow, so first just attempt to login
        // to the closest data center. If that doesnt work, then make the slow call and retry login.

        // Try closest
        configureGigyaForClosestDataCenter({ dc ->
            tryLogin {
                // That didnt work, try datacenter lookup
                configureGigyaForAccountDataCenter(email, dc, { tryLogin() }, { failure(R.string.invalid_user_password) })
            }
        }, { failure(R.string.login_failed) })
    }

    private fun continueAuthentication(auth: GigyaApi.AuthenticationResult, success: () -> Unit, failure: (Int) -> Unit) {
        val body = SessionTokenRequestBody(auth.user.uID, auth.token, auth.secret, gigya.dataCenter)

        api.requestSessionToken(body).call({
            if (it.jwt == null) {
                failure(GigyaApi.Method.LOGIN.genericError)
                return@call
            }

            tokens.set(it.jwt, body.apiKey, body.secret)
            user.setIsLoggedIn(true)
            setUser(auth.user, true)

             local.updateGigyaGlobalizationProperties {
                success()
            }
        }, {
            failure(GigyaApi.Method.LOGIN.genericError)
        })
    }

    /*fun deleteAccount(email: String, success: () -> Unit, failure: (Int) -> Unit) {
        gigyaApi.deleteAccount(email, success, failure)
    }*/

    fun logout() {
        lastProfileRefresh = 0L
        tokens.clearUser()
        user.clear()
        gigyaApi.clearCache()
        gigya.logout()
    }

    fun refreshProfileIfStale() {
        if (tokens.isUserLoggedIn) {
            val now = System.currentTimeMillis()

            if (now - lastProfileRefresh > 60_000L) {
                lastProfileRefresh = now
                refreshProfile()
            }
        }
    }

    fun refreshProfile() {
        if (tokens.isUserLoggedIn) {
            refreshGigyaProfile()
            refreshBackendProfile()
        }
    }

    fun savePreferredDealer(dealerId: String, dealerName: String, success: () -> Unit, failure: (Int) -> Unit) {
        gigyaApi.setDataFields({ preferredDealer = dealerId }, {
            user.updateUser {
                it.preferredDealerID = dealerId
                it.preferredDealerName = dealerName
            }

            success()
        }, failure)
    }

    fun updateProfile(user: User, success: () -> Unit, failure: (Int) -> Unit) {
        gigyaApi.updateProfile(user, {
            setUser(user)

            success()
        }, failure)
    }

    /*fun updateEmail(user: User, newEmail: String, success: () -> Unit, failure: (Int) -> Unit) {
        gigyaApi.updateEmail(user, newEmail, {
            setUser(user)
            sentry.registerUser(tokens.uuid, newEmail)
            updateLambdaLoginID()

            success()
        }, failure)
    }*/

    private fun updateLambdaLoginID() {
        Log.i(TAG, "updateLambdaLoginID")

        val uid = user.profile.value?.uID ?: return
        val uidSignature = user.profile.value?.uidSignature ?: return
        val signatureTimestamp = user.profile.value?.signatureTimestamp ?: return

        dataCenters.updateLoginID(UpdateLoginIDRequest(gigya.apiKey, uid, uidSignature, signatureTimestamp)).emptyCall({
            Log.i(TAG, "Successfully updated Login ID")
        }, {
            val message = "Failed to update Login ID: $it"
            Log.e(TAG, message)
        })
    }

    fun resetPassword(email: String, success: () -> Unit, failure: (Int) -> Unit) {
        // This is same as login flow. Try the closest data center first, if that doesnt work, try all
        fun tryReset(failed: (Int) -> Unit = failure) {
            gigyaApi.resetPassword(email, success, { failed(it) })
        }

        // Try closest
        configureGigyaForClosestDataCenter({ dc ->
            tryReset {
                // That didnt work, try datacenter lookup
                configureGigyaForAccountDataCenter(email, dc, { tryReset() }, { failure(R.string.cant_find_user_create_account_instead) })
            }
        }, {
            failure(GigyaApi.Method.RESET_PASSWORD.genericError)
        })
    }

    fun updatePassword(currentPassword: String, newPassword: String, success: () -> Unit, failure: (Int) -> Unit) {
        gigyaApi.updatePassword(currentPassword, newPassword, success, failure)
    }


    fun uploadPicture(uri: Uri, success: () -> Unit, failure: (Int) -> Unit) {
        gigyaApi.uploadPicture(uri, success, failure)
    }

    fun removePicture(success: () -> Unit, failure: (Int) -> Unit) {
        gigyaApi.removePicture(success, failure)
    }





    private fun refreshGigyaProfile() {
        gigyaApi.refreshProfile({
            setUser(it)
        }, {
            Log.e(TAG, "Failed to refresh gigya profile: $it")
        })
    }

    private fun refreshBackendProfile() {
    }



    private fun setUser(user: User, new: Boolean = false) {
        this.user.setUser(user, new)
    }

    private fun configureGigyaForClosestDataCenter(success: (String) -> Unit, failure: () -> Unit) {
        Log.d(TAG, "Finding closest datacenter")

        harley.getClosestDataCenter().json({
            Log.d(TAG, "Closest datacenter: ${it.DC}")

            val dc = it.DC

            if (dc != null && dc.isNotBlank()) {
                gigya.initialize(dc)

                success(dc)
            } else {
                failure()
            }
        }, {
            failure()
        })
    }

    private fun configureGigyaForAccountDataCenter(email: String, alreadyTriedDC: String, success: () -> Unit, failure: () -> Unit) {
        Log.d(TAG, "Find datacenter")

        getDataCenterForEmail(email, {
            val dc = it.dc

            if (dc != null && dc.isNotBlank() && dc != alreadyTriedDC) {
                gigya.initialize(dc)

                success()
            } else {
                failure()
            }
        }, failure)
    }

    private fun getDataCenterForEmail(email: String, success: (GetAccountDataCenterResponse) -> Unit, failure: () -> Unit) {
        dataCenters.getAccountDataCenter(email).json({
            Log.d(TAG, "Found datacenter ${it.dc} lite ${it.hasLiteAccount} full ${it.hasFullAccount}")

            success(it)
        }, {
            Log.d(TAG, "No datacenter for email")

            failure()
        })
    }
}

