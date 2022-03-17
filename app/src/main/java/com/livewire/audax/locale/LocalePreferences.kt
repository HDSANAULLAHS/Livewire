package com.livewire.audax.locale

import android.content.Context
import androidx.core.content.edit

class LocalePreferences(context: Context) {
    private var preferences = context.getSharedPreferences("locale", Context.MODE_PRIVATE)

    companion object {
        private const val LANGUAGE = "language"
        private const val HAS_SET_LOCALE_AND_REGION = "has_set_locale_and_region"
        private const val REGION = "region"
        private const val MEASUREMENT_SYSTEM = "measurement_system"
        private const val TEMPERATURE_DISPLAY_TYPE = "temperature_display_type"
        private const val LOCALE_TERMS_AND_CONDITIONS = "terms_and_conditions"
        private const val NAVIGATION_VOICE_ID = "navigation_voice_id"
    }

    // The app is killed immediately after setting this preference. Sometimes apply() does not
    // write changes. commit() is used instead for the following locale settings

    var navigationVoiceId: Long
        get() = preferences.getLong(NAVIGATION_VOICE_ID, 206L)
        set(value) { preferences.edit(commit = true) { putLong(NAVIGATION_VOICE_ID, value) }}

    var hasSetLocaleAndRegion: Boolean
        get() = preferences.getBoolean(HAS_SET_LOCALE_AND_REGION, false)
        set(value) { preferences.edit(commit = true) { putBoolean(HAS_SET_LOCALE_AND_REGION, value) }}

    var localeTermsAndConditions: Set<String>
        get() = HashSet(preferences.getStringSet(LOCALE_TERMS_AND_CONDITIONS, null) ?: setOf())
        set(value) { preferences.edit(commit = true) { putStringSet(LOCALE_TERMS_AND_CONDITIONS, value) }}

    var language: String?
        get() = preferences.getString(LANGUAGE, null)
        set(value) { preferences.edit(commit = true) { putString(LANGUAGE, value) }}

    var region: String?
        get() = preferences.getString(REGION, null)
        set(value) { preferences.edit(commit = true) { putString(REGION, value) }}

    var temperatureDisplayType: String?
        get() = preferences.getString(TEMPERATURE_DISPLAY_TYPE, null)
        set(value) { preferences.edit(commit = true) { putString(TEMPERATURE_DISPLAY_TYPE, value) }}

    var measurementSystem: String?
        get() = preferences.getString(MEASUREMENT_SYSTEM, null)
        set(value) { preferences.edit(commit = true) { putString(MEASUREMENT_SYSTEM, value) }}

    val acceptedAnyTermsAndConditions: Boolean
        get() = localeTermsAndConditions.isNotEmpty()

    fun acceptTermsForRegion(regionCode: String) {
        val regions = localeTermsAndConditions.toMutableSet()
        regions.add(regionCode)
        localeTermsAndConditions = regions
    }

    fun termsAcceptedForRegion(regionCode: String): Boolean {
        return localeTermsAndConditions.contains(regionCode)
    }

    fun clear() {
        preferences.edit { clear() }
    }
}
