package com.livewire.app.locale

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.inject

abstract class LocaleAwareActivity: AppCompatActivity() {
    protected val localeManager: LocaleManager by inject()

    override fun attachBaseContext(context: Context?) {
        context?.let {
            super.attachBaseContext(localeManager.setSystemLocale(it))
        }
    }
}
