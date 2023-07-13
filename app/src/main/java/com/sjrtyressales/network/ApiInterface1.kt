package com.sjrtyressales.network

import com.sjrtyressales.screens.splashScreen.model.ModelUpdateLiveLocationRequest
import com.sjrtyressales.screens.splashScreen.model.ModelUpdateLiveLocationResponse
import com.sjrtyressales.utils.Constant
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import java.util.*

interface ApiInterface1 {


    @POST(Constant.updateLiveLocation)
    fun updateLiveLocation(
        @HeaderMap  headers:HashMap<String,String>,
        @Body MessageBody: ModelUpdateLiveLocationRequest?
    ): Call<ModelUpdateLiveLocationResponse>?

    @POST(Constant.updateLiveLocation)
    fun updateLiveLocation1(
        @Body MessageBody: ModelUpdateLiveLocationRequest?
    ): Call<ModelUpdateLiveLocationResponse>?

}