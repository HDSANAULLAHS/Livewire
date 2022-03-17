package com.livewire.audax.authentication.ui.api

import com.livewire.audax.model.*
import retrofit2.Call
import retrofit2.http.*

interface DataCenterApi {
    @GET("hd-lookup")
    fun getAccountDataCenter(@Query("email") emailAddress: String): Call<GetAccountDataCenterResponse>

    @POST("hd-lookup-update-loginId")
    fun updateLoginID(@Body request: UpdateLoginIDRequest): Call<Void>
}
