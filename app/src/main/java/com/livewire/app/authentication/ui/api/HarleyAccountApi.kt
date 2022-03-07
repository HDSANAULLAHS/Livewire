package com.livewire.app.authentication.ui.api

import com.livewire.app.model.*
import retrofit2.Call
import retrofit2.http.*

interface HarleyAccountApi {
    @GET("/hd/prd/countryToApiKey.esi")
    fun getClosestDataCenter(@Header("Content-Type")
                             contentType: String = "application/json") : Call<GetClosestDataCenterResponse>
}
