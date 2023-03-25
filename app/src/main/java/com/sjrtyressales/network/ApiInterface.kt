package com.sjrtyressales.network

import com.sjrtyressales.model.ModelDashboardResponse
import com.sjrtyressales.model.ModelHistoryResponse
import com.sjrtyressales.model.ModelLoginRequest
import com.sjrtyressales.model.ModelLoginResponse
import com.sjrtyressales.utils.Constant
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {

    @POST(Constant.login)
    suspend fun login(@Body body:ModelLoginRequest):Response<ModelLoginResponse>

    @GET(Constant.dashboard)
    suspend fun getDashBoard():Response<ModelDashboardResponse>

    @GET(Constant.meetingHistory)
    suspend fun getMeetingHistory():Response<ModelHistoryResponse>
}