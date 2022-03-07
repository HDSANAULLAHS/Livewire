package com.livewire.app.authentication.ui.api

import com.livewire.app.model.*
import retrofit2.Call
import retrofit2.http.*

interface UserAccountApi {
    @POST("session")
    fun requestSessionToken(@Body body: SessionTokenRequestBody) : Call<SessionTokenRequestResponse>

}
