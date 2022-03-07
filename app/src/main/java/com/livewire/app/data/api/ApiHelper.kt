package com.livewire.app.data.api

import com.livewire.app.data.model.User
import retrofit2.Response

interface ApiHelper {

    suspend fun getUsers(): Response<List<User>>
}