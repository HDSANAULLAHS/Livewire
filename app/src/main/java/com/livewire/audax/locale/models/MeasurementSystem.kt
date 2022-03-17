package com.livewire.audax.locale.models

import com.livewire.audax.R

enum class MeasurementSystem {

    IMPERIAL {
        override val rawValue = "imperial"
        override val titleResource = R.string.measurement_system_type_imperial
        override val titleWithSymbolsResource = R.string.measurement_system_type_imperial_with_symbols
        //override val navigationDistantUnit = R.string.imperial
    },

    METRIC {
        override val rawValue = "metric"
        override val titleResource = R.string.measurement_system_type_metric
        override val titleWithSymbolsResource = R.string.measurement_system_type_metric_with_symbols
        //override val navigationDistantUnit = R.string.metric
    };

    abstract val rawValue : String
    abstract val titleResource : Int
    abstract val titleWithSymbolsResource : Int
    //abstract val navigationDistantUnit : R.string.imperial

    companion object {
        fun getObjectForRawValue(rawValue: String): MeasurementSystem {
            return values().firstOrNull { it.rawValue == rawValue } ?: IMPERIAL
        }
    }
}