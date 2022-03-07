package com.livewire.app.session

import android.content.Context
import androidx.core.content.edit
import com.auth0.android.jwt.DecodeException

class SessionTokenStore(context: Context) {
    private var preferences = context.getSharedPreferences("sessiontoken",
        Context.MODE_PRIVATE)

    companion object {
        private const val JWT = "jwt"
        private const val GIGYA_TOKEN = "gigya_token"
        private const val GIGYA_SECRET = "gigya_sec"
        private const val REGISTRATION_ID = "reg_id"
    }

    init {
        decodeUuid()
    }

    var uuid: String? = null
        private set

    val gigyaToken: String?
        get() = preferences.getString(GIGYA_TOKEN, null)

    val gigyaSecret: String?
        get() = preferences.getString(GIGYA_SECRET, null)

    val jwt: String?
        get() = preferences.getString(JWT, null)

    var registrationId: String?
        get() = preferences.getString(REGISTRATION_ID, null)
        set(value) { preferences.edit { putString(REGISTRATION_ID, value) } }

    fun set(jwt: String, token: String, secret: String) {
        preferences.edit {
            putString(JWT, jwt)
            putString(GIGYA_TOKEN, token)
            putString(GIGYA_SECRET, secret)
        }

        decodeUuid()
    }

    val isUserLoggedIn: Boolean
        get() = !jwt.isNullOrBlank() && !gigyaToken.isNullOrBlank() && !gigyaSecret.isNullOrBlank()

    fun clear() {
        preferences.edit { clear() }
    }

    fun clearUser() {
        preferences.edit {
            remove(JWT)
            remove(GIGYA_TOKEN)
            remove(GIGYA_SECRET)
        }
    }

    private fun decodeUuid() {
        uuid = try {
            val jwt = this.jwt
            if (jwt != null) com.auth0.android.jwt.JWT(jwt).subject else null
        } catch (ex: DecodeException) {
            ex.printStackTrace()
            null
        }
    }
}

