package com.livewire.app.gigya

import android.content.Context
import android.net.Network
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.annotation.Keep
import com.gigya.android.sdk.Gigya
import com.gigya.android.sdk.GigyaCallback
import com.gigya.android.sdk.account.models.Profile
import com.gigya.android.sdk.api.GigyaApiResponse
import com.gigya.android.sdk.network.GigyaError
import com.gigya.android.sdk.network.adapter.RestAdapter
import com.gigya.android.sdk.session.SessionInfo
import com.livewire.app.R
import com.livewire.app.authentication.GigyaManager
import com.livewire.app.model.User
import com.livewire.app.session.SessionTokenStore
import com.livewire.app.store.FileStorage
import com.livewire.app.utils.NetworkHelper
import com.squareup.moshi.Moshi
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.concurrent.thread

fun GigyaApiResponse.getString(field: String): String {
    return this.getField(field, String::class.java) ?: ""
}

open class GigyaApi(val context: Context,
                    private val tokens: SessionTokenStore,
                    private val storage: FileStorage,
                    val network: NetworkHelper,
                    val moshi: Moshi,
                    val gigya: GigyaManager
) {
    companion object {
        const val TAG = "GigyaApi"

        const val USER_DIR = "gigyaCache"
        const val USER_FILE = "profile.json"

        const val ERROR_UNDERAGE_USER = 403044
        const val ERROR_PREVIOUS_PASSWORD = 401030
        const val ERROR_INVALID_USER = 401031
        const val ERROR_INVALID_LOGIN = 403042
        const val ERROR_ACCOUNT_LOCKED = 403120
        const val ERROR_EMAIL_IN_USE = 403043
        const val ERROR_PASSWORD_CHANGE = 403100

        // Error codes that map to specific user visible messages. If we dont have a specific message
        // for an error code, we use the fallback generic message, defined per method (see below)
        val ERROR_MESSAGES = mapOf(
                ERROR_UNDERAGE_USER to R.string.underage_user,
                ERROR_PREVIOUS_PASSWORD to R.string.old_password,
                ERROR_INVALID_LOGIN to R.string.invalid_user_password,
                ERROR_ACCOUNT_LOCKED to R.string.account_locked,
                ERROR_EMAIL_IN_USE to R.string.account_exists
        )
    }

    enum class Method(val action: String, val genericError: Int, val unhandledErrorToSentry: Boolean) {
        LOGIN("accounts.login", R.string.login_failed, true),
        GET_ACCOUNT("accounts.getAccountInfo", R.string.communication_error, false),
        SET_ACCOUNT("accounts.setAccountInfo", R.string.update_account_failed, false),
        CREATE_ACCOUNT("accounts.register", R.string.create_account_failed, true),
        VERIFY_EMAIL("accounts.resendVerificationCode", R.string.communication_error, false),
        INIT_REGISTRATION("accounts.initRegistration", R.string.create_account_failed, true),
        RESET_PASSWORD("accounts.resetPassword", R.string.update_account_failed, false),
        SET_PHOTO("accounts.setProfilePhoto", R.string.cant_upload_picture, false),
        FINALIZE_ACCOUNT("accounts.finalizeRegistration", R.string.login_failed, false)
    }

    @Keep
    class DataPreferences(val temperaturePreference: String? = null,
                          val measurementPreference: String? = null)

    @Keep
    class DataOptIn(val emailSelectedLW: Boolean? = null, val emailModifyDateLW: String? = null)

    @Keep
    class DataHogMember(val memberID: String? = null)

    @Keep
    class DataAddress(val `default`: Boolean? = null,
                      val firstName:String? = null,
                      val lastName:String? = null,
                      val line1: String? = null,
                      val line2: String? = null,
                      val line3: String? = null,
                      val zip: String? = null,
                      val city: String? = null,
                      val state: String? = null,
                      val country: String? = null,
                      val phoneNumber: String? = null
    )

    @Keep
    class DataFields(var termsLW: Boolean? = true,
                     var middleName: String? = null,
                     var preferredDealer: String? = null,
                     var appLocale: String? = null,
                     var appCountry: String? = null,
                     var userPreferences: DataPreferences? = null,
                     var optIn: DataOptIn? = null,
                     var hogMember: DataHogMember? = null,
                     var additionalAddresses: List<DataAddress>? = null,
                     var accountCreatedBrand: String? = "LiveWire",
                     var bikesOwned: List<GigyaBike>? = null,
                     var campaignZip: String? = null,
                     var campaignCountry: String? = null) {
        val address: DataAddress?
            get() = additionalAddresses?.firstOrNull { it.`default` == true }
        var deleteRequestDate: String? = null
    }

    @Keep
    class GigyaBike(var year: Int? = null, var model: String? = null, val make: String? = null, val vin: String? = null)

    class AuthenticationResult(val user: User, val token: String, val secret: String)

    @Keep
    class ErrorFields(var regToken: String?)

    private val profileAdapter = moshi.adapter(Profile::class.java)
    private val dataAdapter = moshi.adapter(DataFields::class.java)
    private val errorAdapter = moshi.adapter(ErrorFields::class.java)

    fun createAccount(user: User, password: String, locale: String, success: (AuthenticationResult) -> Unit, failure: (Int) -> Unit) {

        initAccountRegistration({ regToken ->
            val params = gigyaProfileParamsFromUserModel(user, true, locale = locale)

            params["email"] = user.email
            params["password"] = password
            params["regToken"] = regToken
            params["finalizeRegistration"] = true

            registerAccount(user, params, success, failure)
        }, failure)
    }

    private fun initAccountRegistration(success: (String) -> Unit, failure: (Int) -> Unit) {
        // Gigya Requires we call `accounts.initRegistration` first
        // and get a registration token

        callGigya(Method.INIT_REGISTRATION, mapOf(), {
            success(it.getString("regToken"))
        }, failure)
    }

    fun login(email: String, password: String, success: (AuthenticationResult) -> Unit, failure: (Int) -> Unit, forceChangePassword: (String) -> Unit) {
        val params = mapOf<String, Any>("loginID" to email,
                "password" to password,
                "include" to "profile,data")

        callGigya(Method.LOGIN, RestAdapter.POST, params, {
            success(extractAuthenticationResult(it))
        }, failure, { _, token ->
            forceChangePassword(token)
        })
    }

    fun checkPassword(email: String, password: String, success: (AuthenticationResult) -> Unit, failure: (Int) -> Unit) {
        val params = mapOf<String, Any>("loginID" to email,
                "password" to password,
                "include" to "profile,data")

        callGigya(Method.LOGIN, params, {
            success(extractAuthenticationResult(it))
        }, { code ->

            if (code == ERROR_ACCOUNT_LOCKED) {
                failure(R.string.account_locked)
            } else if (code == ERROR_INVALID_USER || code == ERROR_PREVIOUS_PASSWORD) {
                failure(R.string.invalid_user_password)
            } else {
                failure(R.string.login_failed)
            }
        })
    }
    private fun registerAccount(user: User, params: Map<String, Any>, success: (AuthenticationResult) -> Unit, failure: (Int) -> Unit) {
        callGigya(Method.CREATE_ACCOUNT, params, {
            val result = extractAuthenticationResult(it)

            // Extra fields not returned by create account response
            result.user.middleName = user.middleName
            result.user.emailOptIn = user.emailOptIn

            // Request a verification email as well
            requestVerificationEmail(result.user.uID, result.token)

            success(result)
        }, failure)
    }
    private fun requestVerificationEmail(uid: String, token: String) {
        val params = mapOf("UID" to uid, "regToken" to token)

        callGigya(Method.VERIFY_EMAIL, params, {
            Log.i(TAG, "Request verification email success")
        }, {
            Log.e(TAG, "Request verification email failed: $it")
        })
    }

    fun refreshProfile(success: (user: User) -> Unit, failure: (Int) -> Unit) {
        val token = tokens.gigyaToken ?: return

        val params = mapOf("UID" to token)

        if (!network.isNetworkConnected()) {
            val user = storage.readFromCache<User>(USER_DIR, USER_FILE)
            if (user != null) {
                success(user)
                return
            }
        }

        callGigya(Method.GET_ACCOUNT, params, {
            success(extractUserFromResponse(it))
        }, failure)
    }

    fun setDataFields(setter: DataFields.() -> Unit, success: () -> Unit, failure: (Int) -> Unit) {
        setDataFields(DataFields().apply(setter), tokens.gigyaToken, tokens.gigyaSecret, success, failure)
    }

    fun setDataFields(data: DataFields, success: () -> Unit, failure: (Int) -> Unit) {
        setDataFields(data, tokens.gigyaToken, tokens.gigyaSecret, success, failure)
    }

    fun setDataFields(data: DataFields, token: String?, secret: String?, success: () -> Unit, failure: (Int) -> Unit) {
        if (token == null || secret == null) {
            failure(R.string.update_account_failed)
            return
        }

        val params = mapOf(
                "UID" to token,
                "secret" to secret,
                "data" to dataAdapter.toJson(data))

        callGigya(Method.SET_ACCOUNT, params, { success() }, failure)
    }

    fun updatePassword(currentPassword: String, newPassword: String, success: () -> Unit, failure: (Int) -> Unit) {
        val token = tokens.gigyaToken ?: return
        val secret = tokens.gigyaSecret ?: return

        val params = mapOf("UID" to token,
                "secret" to secret,
                "password" to currentPassword,
                "newPassword" to newPassword)

        callGigya(Method.SET_ACCOUNT, params, { success() }, failure)
    }

    fun changePasswordForced(regToken: String, currentPassword: String, newPassword: String, success: (AuthenticationResult) -> Unit, failure: (Int) -> Unit) {
        val params = mapOf("regToken" to regToken,
                "password" to currentPassword,
                "newPassword" to newPassword)

        callGigya(Method.SET_ACCOUNT, RestAdapter.POST, params, {
            callGigya(Method.FINALIZE_ACCOUNT, RestAdapter.POST, mapOf("regToken" to regToken), {
                val result = extractAuthenticationResult(it)

                success(result)
            }, failure)
        }, failure)
    }

    fun updateProfile(user: User, success: () -> Unit, failure: (Int) -> Unit) {
        val token = tokens.gigyaToken ?: return
        val secret = tokens.gigyaSecret ?: return

        var fromCreate = false

        if (user.addressCity.isNullOrEmpty() || user.addressLine1.isNullOrEmpty() || user.addressState.isNullOrEmpty())
            fromCreate = true


        val params = gigyaProfileParamsFromUserModel(user, fromCreate)

        params["UID"] = token
        params["secret"] = secret

        callGigya(Method.SET_ACCOUNT, params, {
            success()
        }, failure)
    }

    /*fun updateEmail(user: User, newEmail: String, success: () -> Unit, failure: (Int) -> Unit) {
        val token = tokens.gigyaToken ?: return
        val secret = tokens.gigyaSecret ?: return

        val profile = Profile().apply {
            this.email = newEmail
        }

        val params = mutableMapOf("profile" to profileAdapter.toJson(profile))

        params["UID"] = token
        params["secret"] = secret
        params["addLoginEmails"] = newEmail
        params["removeLoginEmails"] = user.email

        callGigya(Method.SET_ACCOUNT, params, {
            user.email = newEmail
            success()
        }, failure)
    }*/

    fun resetPassword(email: String, success: () -> Unit, failure: (Int) -> Unit) {
        callGigya(Method.RESET_PASSWORD, mapOf("loginID" to email), {
            success()
        }, failure)
    }

    fun uploadPicture(uri: Uri, success: () -> Unit, failure: (Int) -> Unit) {
        thread {
            val imageString = getFileAsEncodedString(uri)
            if (imageString == null) {
                failure(-1)
                return@thread
            }

            callGigya(Method.SET_PHOTO, RestAdapter.POST, mapOf("photoBytes" to imageString, "publish" to "true"), {
                success()
            }, failure)
        }
    }

    /*fun deleteAccount(email: String, success: () -> Unit, failure: (Int) -> Unit) {
        val token = tokens.gigyaToken ?: return
        val secret = tokens.gigyaSecret ?: return

        val data = DataFields()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        data.deleteRequestDate = dateFormat.format(Date())
        data.accountCreatedBrand = null
        data.termsLW = null

        val params = mapOf("UID" to token,
                "secret" to secret,
                "removeLoginEmails" to email,
                "data" to dataAdapter.toJson(data))

        callGigya(Method.SET_ACCOUNT, params, {
            success()
        }, failure)
    }*/

    private fun getFileAsEncodedString(uri: Uri): String? {
        val stream = context.contentResolver.openInputStream(uri) ?: return null

        stream.use { content ->
            ByteArrayOutputStream().use { output ->
                content.copyTo(output)

                return Base64.encodeToString(output.toByteArray(), Base64.DEFAULT)
            }
        }
    }

    fun removePicture(success: () -> Unit, failure: (Int) -> Unit) {
        val token = tokens.gigyaToken ?: return
        val params = hashMapOf<String, Any>(
                "UID" to token,
                "profile" to "{photoURL: null, thumbnailURL: null}")

        callGigya(Method.SET_ACCOUNT, params, {
            success()
        }, failure)
    }

    private fun callGigya(method: Method, params: Map<String, Any>, success: (GigyaApiResponse) -> Unit, failure: (Int) -> Unit) {
        callGigya(method, RestAdapter.GET, params, success, failure)
    }

    private fun callGigya(method: Method, verb: Int, params: Map<String, Any>, success: (GigyaApiResponse) -> Unit, failure: (Int) -> Unit, failureWithRegToken: ((Int, String) -> Unit)? = null) {
        val responseListener = object : GigyaCallback<GigyaApiResponse>() {
            override fun onSuccess(response: GigyaApiResponse) {
                Log.w(TAG, "Gigya call success: ${method.action}")
                Log.e("GigyaApiResponse", ""  + response.asJson())
                success(response)
            }

            override fun onError(error: GigyaError) {
                Log.w(TAG, "Gigya called failed: ${method.action}")
                Log.w(TAG, error.toString())

                // In the future also handle expired authentication here
                if (error.errorCode == ERROR_PASSWORD_CHANGE && failureWithRegToken != null) {
                    errorAdapter.fromJson(error.data)!!.regToken?.let { token ->
                        failureWithRegToken(error.errorCode, token)
                        return
                    }
                }

                // Handle error and return message
                val message = handleError(method, error.errorCode, error.callId)
                failure(message)
            }
        }

        Gigya.getInstance().send(method.action, params, verb, GigyaApiResponse::class.java, responseListener)
    }

    private fun gigyaProfileParamsFromUserModel(user: User, fromCreate: Boolean, locale: String? = null): MutableMap<String, Any> {
        if (fromCreate) {
            val profile = Profile().apply {
                this.firstName = user.firstName
                this.lastName = user.lastName
                this.birthDay = user.birthDay
                this.birthMonth = user.birthMonth
                this.birthYear = user.birthYear
                this.locale = locale
            }

            val data = DataFields(
                    termsLW = true,
                    campaignZip = user.addressZip,
                    campaignCountry = "US",
                    optIn = DataOptIn(emailSelectedLW = user.emailOptIn, DateTime.now().withZone(
                        DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ssZ")),
                    middleName = user.middleName
            )

            return mutableMapOf("profile" to profileAdapter.toJson(profile),
                    "data" to dataAdapter.toJson(data))
        }
        else {
            val profile = Profile().apply {
                this.firstName = user.firstName
                this.lastName = user.lastName
                this.birthDay = user.birthDay
                this.birthMonth = user.birthMonth
                this.birthYear = user.birthYear
                this.locale = locale
            }
            val data = DataFields(
                    termsLW = true,
                    optIn = DataOptIn(emailSelectedLW = user.emailOptIn, DateTime.now().withZone(DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ssZ")),
                    middleName = user.middleName,
                    additionalAddresses = listOf(
                            DataAddress(
                                    default = true,
                                    line1 = user.addressLine1,
                                    line2 = user.addressLine2,
                                    line3 = null,
                                    zip = user.addressZip,
                                    city = user.addressCity,
                                    state = user.addressState,
                                    country = "US",
                                    phoneNumber = user.mobilePhoneNumber
                            )
                    )
            )

            return mutableMapOf("profile" to profileAdapter.toJson(profile),
                    "data" to dataAdapter.toJson(data))
        }
    }

    private fun extractAuthenticationResult(response: GigyaApiResponse): AuthenticationResult {
        val token = response.getString("sessionInfo.sessionToken")
        val secret = response.getString("sessionInfo.sessionSecret")
        val user = extractUserFromResponse(response)

        Gigya.getInstance().setSession(SessionInfo(secret, token))

        return AuthenticationResult(user, token, secret)
    }

    private fun extractUserFromResponse(response: GigyaApiResponse): User {
        val profile = response.getField("profile", Profile::class.java)
        val data = response.getField("data", DataFields::class.java)

        val user = User(
                uID = response.getString("UID"),
                uidSignature = response.getString("UIDSignature"),
                signatureTimestamp = response.getString("signatureTimestamp"),
                addressLine1 = data?.additionalAddresses?.getOrNull(0)?.line1 ?: "",
                addressLine2 = data?.additionalAddresses?.getOrNull(0)?.line2 ?: "",
                addressLine3 = data?.additionalAddresses?.getOrNull(0)?.line3 ?: "",
                addressZip = data?.additionalAddresses?.getOrNull(0)?.zip ?: "",
                addressCity = data?.additionalAddresses?.getOrNull(0)?.city ?: "",
                addressState = data?.additionalAddresses?.getOrNull(0)?.state ?: "",
                addressCountry = data?.additionalAddresses?.getOrNull(0)?.country ?: "",
                firstName = profile?.firstName ?: "",
                middleName = data?.middleName ?: "",
                lastName = profile?.lastName ?: "",
                photoURL = profile?.photoURL ?: "",
                birthDay = profile?.birthDay ?: 0,
                birthMonth = profile?.birthMonth ?: 0,
                birthYear = profile?.birthYear ?: 0,
                email = profile?.email ?: "",
                preferredDealerID = data?.preferredDealer ?: "",
                hogMemberID = data?.hogMember?.memberID ?: "",
                emailOptIn = data?.optIn?.emailSelectedLW ?: false,
                mobilePhoneNumber = data?.additionalAddresses?.getOrNull(0)?.phoneNumber ?: "",
                bikesOwned = data?.bikesOwned
        )

        storage.writeToCache(USER_DIR, USER_FILE, user)

        return user
    }

    fun clearCache() {
        storage.deleteFromCache(USER_DIR, USER_FILE)
    }

    fun handleError(method: Method, code: Int, callID: String?): Int {
        // If we have a known error message for this code, return that and done
        val translated = ERROR_MESSAGES[code]
        if (translated != null) {
            return translated
        }

        // Log to sentry, if needed
        if (method.unhandledErrorToSentry) {
            logError(code, method.action, callID)
        }

        // Generic fallback error for this method
        return method.genericError
    }

    private fun logError(code: Int, method: String, callID: String?) {
        val extras = mapOf(
                "action" to method,
                "errorCode" to code.toString(),
                "dataCenter" to gigya.dataCenter,
                "callId" to (callID ?: "")
        )
    }
}
