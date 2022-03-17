package com.livewire.audax.locale.models

import com.livewire.audax.R
import java.util.*

enum class Language {

    ENGLISH_USA {
        override val locale = Locale.US
        override val darkSkyLanguageCode = "en"
        override val gigyaAppLocaleCode = "en-US"
        override val gigyaLocaleCode = "en"
        override val titleResource = R.string.language_english_usa
    };

    abstract val locale: Locale
    abstract val darkSkyLanguageCode: String
    abstract val gigyaAppLocaleCode: String
    abstract val gigyaLocaleCode: String
    abstract val titleResource: Int

    val backEndLanguageCode
        get() = gigyaAppLocaleCode
}
