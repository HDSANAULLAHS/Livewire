package com.livewire.audax.locale.models

import com.livewire.audax.R

enum class TemperatureDisplayType {

    FAHRENHEIT {
        override val rawValue = "fahrenheit"
        override val titleResource = R.string.temperature_display_type_fahrenheit
        override val titleWithSymbolsResource = R.string.temperature_display_type_fahrenheit_with_symbols
    },

    CELSIUS {
        override val rawValue = "celsius"
        override val titleResource = R.string.temperature_display_type_celsius
        override val titleWithSymbolsResource = R.string.temperature_display_type_celsius_with_symbols
    };

    abstract val rawValue : String
    abstract val titleResource : Int
    abstract val titleWithSymbolsResource : Int

    companion object {
        fun getObjectForRawValue(rawValue: String): TemperatureDisplayType {
            return values().firstOrNull { it.rawValue == rawValue } ?: FAHRENHEIT
        }
    }
}
