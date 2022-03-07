package com.livewire.app.authentication

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.gigya.android.sdk.Gigya
import com.gigya.android.sdk.session.SessionInfo
import com.livewire.app.BuildConfig
import com.livewire.app.session.SessionTokenStore

class GigyaManager(val context: Context,
                   val tokens: SessionTokenStore
) {
    /**
     * Data Center and API key constants
     */
    companion object {
        private const val TAG = "Gigya"

        const val DEFAULT_DATA_CENTER = "us1"
        const val KEY_CURRENT_DC = "datacenter"

        private val DOMAINS = mapOf(
            "us1" to BuildConfig.GIGYA_DOMAIN_US,
            "au1" to BuildConfig.GIGYA_DOMAIN_AU,
            "eu1" to BuildConfig.GIGYA_DOMAIN_EU,
            "cn1" to BuildConfig.GIGYA_DOMAIN_CN,
            "ru1" to BuildConfig.GIGYA_DOMAIN_RU
        )

        private val API_KEYS = mapOf(
            "us1" to BuildConfig.GIGYA_API_KEY_US,
            "au1" to BuildConfig.GIGYA_API_KEY_AU,
            "eu1" to BuildConfig.GIGYA_API_KEY_EU,
            "cn1" to BuildConfig.GIGYA_API_KEY_CN,
            "ru1" to BuildConfig.GIGYA_API_KEY_RU
        )
    }

    private var lastInitializedDataCenter = ""
    private val preferences: SharedPreferences = context.getSharedPreferences("gigya_settings", 0)

    var dataCenter: String
        get() = preferences.getString(KEY_CURRENT_DC, DEFAULT_DATA_CENTER) ?: DEFAULT_DATA_CENTER
        private set(value) = preferences.edit().putString(KEY_CURRENT_DC, value).apply()

    val apiKey: String
        get() = getApiKeyForDataCenter(dataCenter)

    private fun getApiKeyForDataCenter(dc: String): String {
        return API_KEYS[dc] ?:
        API_KEYS[DEFAULT_DATA_CENTER] ?:
        ""
    }

    fun initialize(application: Application) {
        Gigya.setApplication(application)

        if (dataCenter == "") {
            dataCenter = DEFAULT_DATA_CENTER
        }

        initialize(dataCenter)
    }

    fun initialize(dataCenter: String) {
        Log.d(TAG, "Initializing with data center: $dataCenter")

        setupGigyaSession()

        // If we already initialized at this DC do nothing
        if (dataCenter == this.lastInitializedDataCenter) {
            Log.d(TAG, "Already initialized")
            return
        }

        // Get domainkey for data center
        val domain = when {
            DOMAINS.containsKey(dataCenter) -> DOMAINS[dataCenter]
            else -> {
                Log.w(TAG, "Unknown data center: $dataCenter")
                DOMAINS[DEFAULT_DATA_CENTER]
            }
        }

        // Reset (must have called initialize first or this will throw)
        if (this.lastInitializedDataCenter != "") {
            Log.d(TAG, "Logout")
            logout()
        }

        // Initialize for this data center
        Log.d(TAG, "Initialize")
        Gigya.getInstance().logout()
        Gigya.getInstance().init(getApiKeyForDataCenter(dataCenter), domain ?: "")
        setupGigyaSession()

        // Store
        this.lastInitializedDataCenter = dataCenter
        this.dataCenter = dataCenter
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
