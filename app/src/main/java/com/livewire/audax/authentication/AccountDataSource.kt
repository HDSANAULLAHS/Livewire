package com.livewire.audax.authentication

import android.net.Uri
import android.util.Log
import com.livewire.audax.R
import com.livewire.audax.authentication.ui.api.UserAccountApi
import com.livewire.audax.gigya.GigyaApi
import com.livewire.audax.locale.LocaleManager
import com.livewire.audax.model.*
import com.livewire.audax.session.SessionTokenStore
import com.livewire.audax.store.*

// TODO this class is UserAuthentication, Session.)

open class AccountDataSource(val api: UserAccountApi,
                             val gigya: GigyaManager,
                             val gigyaApi: GigyaApi,
                             val tokens: SessionTokenStore,
                             val user: UserViewModel,
                             val local : LocaleManager,
                             val preferences : appPreferences, ) : BaseRemoteDataSource() {
    companion object {
        val TAG = "UserAccountDataSource"
    }

    private var lastProfileRefresh = 0L


    fun createAccount(user: User, password: String, success: () -> Unit, failure: (Int) -> Unit) {
        gigya.initialize()
            continueCreateAccount(user, password, success, failure)

    }

    private fun continueCreateAccount(user: User, password: String, success: () -> Unit, failure: (Int) -> Unit) {
        gigyaApi.createAccount(user, password, { continueAuthentication(it, success, {
            failure(GigyaApi.Method.CREATE_ACCOUNT.genericError) })
        }, failure)
    }

    fun login(email: String, password: String, success: () -> Unit, failure: (Int) -> Unit, changePasswordRequired: (String) -> Unit) {
        // Helper to wrap login and continue
        fun tryLogin(failed: (Int) -> Unit = failure) {
            gigyaApi.login(email, password, { continueAuthentication(it, success, failure) }, failed, changePasswordRequired)
        }

        failure(GigyaApi.Method.LOGIN.genericError)

    }

    fun checkPassword(email: String, password: String, success: () -> Unit, failure: (Int) -> Unit) {
        // Helper to wrap login and continue
        fun tryLogin(failed: (Int) -> Unit = failure) {
            gigyaApi.checkPassword(email, password, { success() }, { failed(it) })
        }

        failure(R.string.login_failed)
    }

    private fun continueAuthentication(auth: GigyaApi.AuthenticationResult, success: () -> Unit, failure: (Int) -> Unit) {
        val body = SessionTokenRequestBody(auth.user.uID, auth.token, auth.secret)

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

    fun logout() {
        lastProfileRefresh = 0L
        tokens.clearUser()
        user.clear()
        gigyaApi.clearCache()
        gigya.logout()
    }

    fun updateProfile(user: User, success: () -> Unit, failure: (Int) -> Unit) {
        gigyaApi.updateProfile(user, {
            setUser(user)

            success()
        }, failure)
    }

    fun resetPassword(email: String, success: () -> Unit, failure: (Int) -> Unit) {
        // This is same as login flow. Try the closest data center first, if that doesnt work, try all
        fun tryReset(failed: (Int) -> Unit = failure) {
            gigyaApi.resetPassword(email, success, { failed(it) })
        }

        failure(GigyaApi.Method.RESET_PASSWORD.genericError)
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

    private fun setUser(user: User, new: Boolean = false) {
        this.user.setUser(user, new)
    }
}

