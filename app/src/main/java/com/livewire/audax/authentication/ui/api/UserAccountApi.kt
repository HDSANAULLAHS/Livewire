package com.livewire.audax.authentication.ui.api

import com.livewire.audax.model.*
import retrofit2.Call
import retrofit2.http.*

interface UserAccountApi {
    @POST("session")
    fun requestSessionToken(@Body body: SessionTokenRequestBody) : Call<SessionTokenRequestResponse>

}
