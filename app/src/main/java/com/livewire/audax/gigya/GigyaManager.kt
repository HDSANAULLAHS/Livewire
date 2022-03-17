package com.livewire.audax.authentication

import android.app.Application
import android.content.Context
import android.util.Log
import com.gigya.android.sdk.Gigya
import com.gigya.android.sdk.session.SessionInfo
import com.livewire.audax.BuildConfig
import com.livewire.audax.session.SessionTokenStore

class GigyaManager(val context: Context,
                   val tokens: SessionTokenStore
) {

    companion object {
        private const val TAG = "Gigya"

        private val DOMAINS = BuildConfig.GIGYA_DOMAIN_GLOBAL
        private val API_KEYS =BuildConfig.GIGYA_API_KEY_GLOBAL

    }

    val apiKey: String
        get() = API_KEYS.toString()

    fun initialize(application: Application) {
        Gigya.setApplication(application)


        initialize()
    }

    fun initialize() {
        setupGigyaSession()

        Log.d(TAG, "Initialize")
        Gigya.getInstance().init(API_KEYS, DOMAINS)
        Log.e("Key:", API_KEYS)
        Log.e("Domain:", DOMAINS)
        println(Gigya.getInstance())
        setupGigyaSession()
    }

    private fun setupGigyaSession() {
        if (tokens.isUserLoggedIn) {
            Gigya.getInstance().setSession(SessionInfo(tokens.gigyaSecret, tokens.gigyaToken))
        }
    }

    fun logout() {
        Gigya.getInstance().logout()
    }
}
