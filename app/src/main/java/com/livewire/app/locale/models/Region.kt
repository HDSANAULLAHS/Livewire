package com.livewire.app.locale.models

import com.livewire.app.R
import java.util.*

enum class Region {


    USA {
        override val regionCode = "us"
        override val locale = Locale.US
        override val titleResource = R.string.region_usa
        override val defaultLanguage = Language.ENGLISH_USA
        override val defaultMeasurementSystem = MeasurementSystem.IMPERIAL
        override val defaultTemperatureType = TemperatureDisplayType.FAHRENHEIT
    };

    abstract val regionCode : String
    abstract val locale: Locale
    abstract val titleResource : Int
    open val defaultMeasurementSystem = MeasurementSystem.METRIC
    open val defaultTemperatureType = TemperatureDisplayType.CELSIUS
    open val defaultLanguage: Language? = null
    open val defaultLanguageFrom = arrayOf<Pair<Language, Language>>()

    fun suggestLanguage(currentLanguage: Language?): Language? {
        // Only return a suggestion if it is different from the current
        val suggestedLanguage = findSuggestedLanguage(currentLanguage)

        return if (suggestedLanguage != currentLanguage) suggestedLanguage else null
    }

    private fun findSuggestedLanguage(currentLanguage: Language?): Language? {
        // First check if there is a mapping from the current language
        val mapping = defaultLanguageFrom.firstOrNull { it.first == currentLanguage }
        if (mapping != null) {
            return mapping.second
        }

        // Use default language, if any
        return defaultLanguage
    }

    companion object {
        fun getObjectForRawValue(rawValue: String?): Region {
            return values().firstOrNull { it.regionCode == rawValue } ?: USA
        }
    }
}
