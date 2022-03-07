package com.livewire.app.locale

import android.content.Context
import android.content.res.Configuration
import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.livewire.app.gigya.GigyaApi
import com.livewire.app.locale.models.Language
import com.livewire.app.locale.models.MeasurementSystem
import com.livewire.app.locale.models.Region
import com.livewire.app.locale.models.TemperatureDisplayType
import java.io.Serializable
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

open class LocaleManager(
    private val preferences: LocalePreferences,
    val gigyaApi: GigyaApi,
    val context: Context
) {
    private val _region = MutableLiveData<Region>()
    private val _measurementSystem = MutableLiveData<MeasurementSystem>()
    private val _temperatureDisplayType = MutableLiveData<TemperatureDisplayType>()

    val region: LiveData<Region>
        get() = _region

    val measurementSystem: LiveData<MeasurementSystem>
        get() = _measurementSystem

    val temperatureDisplayType: LiveData<TemperatureDisplayType>
        get() = _temperatureDisplayType

    val tag = "LocaleManager"

    val acceptedAnyTermsAndConditions: Boolean
        get() = preferences.acceptedAnyTermsAndConditions

    val decimalFormatterForLocale: NumberFormat by lazy {
        // When language changes the app restarts, so this formatter can be cached
        DecimalFormat.getInstance(getCurrentLanguage().locale)
    }

    fun saveTermsAndConditionsAccepted(region: Region) {
        preferences.acceptTermsForRegion(region.regionCode)
    }

    fun checkIfTermsAndConditionsAcceptedForCurrentRegion(): Boolean {
        return preferences.termsAcceptedForRegion(getCurrentRegion().regionCode)
    }

    val use24HTime: Boolean by lazy {
        // TODO The call we are making here also checks the user settings in system settings.
        // We could also call is24HourLocale here which does not check that.
        DateFormat.is24HourFormat(wrapContextForLocale(context, getCurrentRegion().locale))
    }

    fun isMondayTheStartOfTheWeek(): Boolean {
        return false
    }

    val localeContext: Context by lazy {
        wrapContextForLocale(context, getCurrentLanguage().locale)
    }

    val isInUSA: Boolean
        get() = region.value == Region.USA

    val darkSkyUnitsCode: String
        get() = when (getCurrentTemperatureDisplayType()) {
            TemperatureDisplayType.CELSIUS -> "si"
            TemperatureDisplayType.FAHRENHEIT -> "us"
        }

    fun initialize() {
        _region.value = getCurrentRegion()
        _measurementSystem.value = getCurrentMeasurementSystem()
        _temperatureDisplayType.value = getCurrentTemperatureDisplayType()

        // If user set already, done
        if (preferences.hasSetLocaleAndRegion) {
            setSystemLocale(context)
            return
        }

        // Update to default to system locale
        val systemLocale = Locale.getDefault()

        // Try to find one matching language and country, else fall back to just matching language
        val language = Language.values().firstOrNull { it.locale.language.compareTo(systemLocale.language, true) == 0 && it.locale.country.compareTo(systemLocale.country, true) == 0 }
                ?: Language.values().firstOrNull { it.locale.language.compareTo(systemLocale.language, true) == 0 }
                ?: Language.ENGLISH_USA

        setLanguage(language, languageChanged = false)

        // Try to set region as well if possible
        val region = Region.values().firstOrNull { it.locale.country.compareTo(systemLocale.country, true) == 0 }
                ?: Region.USA
        setRegion(region, regionChanged = false)

        setSystemLocale(context)
    }

    // Language

    fun setLanguage(language: Language, languageChanged: Boolean = true, completion: (() -> Unit)? = null) {
        if (languageChanged) {
        }
        preferences.language = language.locale.toString()
        preferences.hasSetLocaleAndRegion = true

        gigyaApi.setDataFields({ appLocale = language.gigyaAppLocaleCode }, {
            Log.i(tag, "Gigya App Locale Set")
            completion?.invoke()
        }, {
            Log.e(tag, "Gigya App Locale Set Failed")
            completion?.invoke()
        })
    }

    fun getCurrentLanguage(): Language {
        val rawValue = preferences.language
        val result = Language.values().firstOrNull { it.locale.toString() == rawValue }
        return result ?: Language.ENGLISH_USA
    }

    fun getLanguages(): Array<Language> {
        return Language.values().sortedBy { context.getString(it.titleResource) }.toTypedArray()
    }

    // Region

    fun setRegion(region: Region, language: Language? = null, regionChanged: Boolean = true, completion: (() -> Unit)? = null) {
        if (regionChanged) {
        }
        preferences.region = region.regionCode

        if (language != null) {
            preferences.language = language.locale.toString()
        }

        preferences.hasSetLocaleAndRegion = true
        _region.value = region

        gigyaApi.setDataFields({
            appCountry = region.regionCode.toUpperCase()

            language?.let {
                appLocale = it.gigyaAppLocaleCode
            }
        }, {
            Log.i(tag, "Gigya App Country Set - ${region.regionCode.toUpperCase()}")
            completion?.invoke()
        }, {
            Log.e(tag, "Gigya App Country Set Failed")
            completion?.invoke()
        })
    }

    fun getCurrentRegion(): Region {
        return Region.getObjectForRawValue(preferences.region)
    }

    fun confirmedLocaleAndRegion() {
        preferences.hasSetLocaleAndRegion = true
    }

    fun getRegions(): Array<Region> {
        return Region.values().sortedBy { context.getString(it.titleResource) }.toTypedArray()
    }

    fun regionIsUS(): Boolean {
        return when (getCurrentRegion()) {
            Region.USA -> true
            else -> false
        }
    }


    // Temperature

    fun setTemperatureDisplayStyle(style: TemperatureDisplayType) {
        preferences.temperatureDisplayType = style.rawValue
        _temperatureDisplayType.value = style

        gigyaApi.setDataFields({ GigyaApi.DataPreferences(temperaturePreference = style.rawValue) }, {
            Log.i(tag, "Gigya Temperature Preference Set - ${style.rawValue}")
        }, {
            Log.e(tag, "Gigya Temperature Preference Set Failed")
        })
    }

    fun getCurrentTemperatureDisplayType(): TemperatureDisplayType {
        val setting = preferences.temperatureDisplayType

        return when (setting) {
            null -> TemperatureDisplayType.FAHRENHEIT
            else -> TemperatureDisplayType.getObjectForRawValue(setting)
        }
    }

    fun getTemperatureDisplayStyles(): Array<TemperatureDisplayType> {
        return TemperatureDisplayType.values()
    }

    // Measurement System

    fun setMeasurementSystem(system: MeasurementSystem) {
        preferences.measurementSystem = system.rawValue
        _measurementSystem.value = system

        gigyaApi.setDataFields({ GigyaApi.DataPreferences(measurementPreference = system.rawValue) }, {
            Log.i(tag, "Gigya Measurement Preference Set - ${system.rawValue}")
        }, {
            Log.e(tag, "Gigya Measurement Preference Set Failed")
        })
    }

    fun getCurrentMeasurementSystem(): MeasurementSystem {
        val setting = preferences.measurementSystem

        return when (setting) {
            null -> MeasurementSystem.IMPERIAL
            else -> MeasurementSystem.getObjectForRawValue(setting)
        }
    }

    fun getMeasurementSystems(): Array<MeasurementSystem> {
        return MeasurementSystem.values()
    }

    fun setSystemLocale(context: Context): Context {
        // TODO we shouldnt need to do this? Locale.getDefault should not be used. And then could
        // use wrapContextForLocale instead
        val locale = getCurrentLanguage().locale
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

        return context.createConfigurationContext(configuration)
    }

    private fun wrapContextForLocale(context: Context, locale: Locale): Context {
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }

        return context.createConfigurationContext(configuration)
    }

    fun getHDSupportNumber(): String {

        return when (region.value) {
            Region.USA -> "1-866-895-4707"
            else -> "+1 414-343-4736"
        }
    }

    fun updateGigyaGlobalizationProperties(completed: () -> Unit = {}) {
        gigyaApi.setDataFields(GigyaApi.DataFields(
                appLocale = getCurrentLanguage().gigyaAppLocaleCode,
                appCountry = getCurrentRegion().regionCode.toUpperCase(),
                userPreferences = GigyaApi.DataPreferences(
                        measurementPreference = getCurrentMeasurementSystem().rawValue,
                        temperaturePreference = getCurrentTemperatureDisplayType().rawValue
                )
        ), completed, { completed() })
    }
}
