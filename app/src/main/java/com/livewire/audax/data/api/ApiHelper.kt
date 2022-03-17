package com.livewire.audax.data.api

import com.livewire.audax.data.model.User
import retrofit2.Response

interface ApiHelper {

    suspend fun getUsers(): Response<List<User>>
}