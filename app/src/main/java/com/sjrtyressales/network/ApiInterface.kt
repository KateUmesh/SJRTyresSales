package com.sjrtyressales.network

import com.sjrtyressales.model.*
import com.sjrtyressales.utils.Constant
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    @POST(Constant.login)
    suspend fun login(@Body body:ModelLoginRequest):Response<ModelLoginResponse>

    @GET(Constant.dashboard)
    suspend fun getDashBoard():Response<ModelDashboardResponse>

    @GET(Constant.meetingHistory)
    suspend fun getMeetingHistory():Response<ModelHistoryResponse>

    @POST(Constant.startMeeting)
    suspend fun startMeeting(@Body body:ModelStartMeetingRequest):Response<ModelStartMeetingResponse>

    @GET(Constant.attendance)
    suspend fun getAttendance():Response<ModelAttendanceResponse>

    @GET(Constant.submitInTime)
    suspend fun submitInTime():Response<ModelSubmitInOutTimeResponse>

    @GET(Constant.submitOutTime)
    suspend fun submitOutTime():Response<ModelSubmitInOutTimeResponse>

    @Multipart
    @POST(Constant.updateProfilePhoto)
    suspend fun uploadProfilePhoto(@Part photo: MultipartBody.Part):Response<ModelProfilePhotoResponse>

    @Multipart
    @POST(Constant.allowance)
    suspend fun postAllowance(@Part photo:MultipartBody.Part
                                       ,@Part("title") title: RequestBody
                                       ,@Part("amount") amount: RequestBody
    ):Response<ModelAllowanceResponse>

}