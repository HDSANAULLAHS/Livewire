package com.livewire.audax.profile.ui.profile

import androidx.lifecycle.ViewModel
import com.livewire.audax.authentication.AccountDataSource
import okhttp3.OkHttpClient

class ProfileViewModel (private val dataSource: AccountDataSource,
                        private val httpAll: OkHttpClient,
                        private val httpConditional: OkHttpClient
): ViewModel() {
    // TODO: Implement the ViewModel



    fun logout() {
        dataSource.logout()
        httpAll.cache?.evictAll()
        httpConditional.cache?.evictAll()
    }
}