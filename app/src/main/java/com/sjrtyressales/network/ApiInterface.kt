package com.sjrtyressales.network

import com.sjrtyressales.model.*
import com.sjrtyressales.utils.Constant
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    @POST(Constant.login)
    suspend fun login(@Body body: ModelLoginRequest): Response<ModelLoginResponse>

    @GET(Constant.dashboard)
    suspend fun getDashBoard(): Response<ModelDashboardResponse>

    @GET(Constant.meetingHistory)
    suspend fun getMeetingHistory(): Response<ModelHistoryResponse>

    @POST(Constant.startMeeting)
    suspend fun startMeeting(@Body body: ModelStartMeetingRequest): Response<ModelStartMeetingResponse>

    @GET(Constant.attendance)
    suspend fun getAttendance(): Response<ModelAttendanceResponse>

    @GET(Constant.submitInTime)
    suspend fun submitInTime(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): Response<ModelSubmitInOutTimeResponse>

    @GET(Constant.submitOutTime)
    suspend fun submitOutTime(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): Response<ModelSubmitInOutTimeResponse>

    @Multipart
    @POST(Constant.updateProfilePhoto)
    suspend fun uploadProfilePhoto(@Part photo: MultipartBody.Part): Response<ModelProfilePhotoResponse>

    @Multipart
    @POST(Constant.allowance)
    suspend fun postAllowance(
        @Part photo: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody
    ): Response<ModelAllowanceResponse>

    @GET(Constant.viewMeetingDetails)
    suspend fun viewMeetingDetails(@Path("meeting_id") value: Int): Response<ModelViewMeetingDetailsResponse>

    @GET(Constant.checkMeetingNotEnd)
    suspend fun checkMeetingNotEnd(): Response<ModelCheckMeetingNotEndResponse>


    @Multipart
    @POST(Constant.endMeeting)
    suspend fun endMeeting(
        @Part photo: MultipartBody.Part,
        @Part("id_meeting") id_meeting: RequestBody,
        @Part("distributor_name") distributor_name: RequestBody,
        @Part("distributor_mobile") distributor_mobile: RequestBody,
        @Part("meeting_conclusion") meeting_conclusion: RequestBody,
        @Part("meeting_end_latitude") meeting_end_latitude: RequestBody,
        @Part("meeting_end_longitude") meeting_end_longitude: RequestBody
    ): Response<ModelEndMeetingResponse>

    @GET(Constant.myProfile)
    suspend fun myProfile(): Response<ModelMyProfileResponse>

    @POST(Constant.updateLiveLocation)
    suspend fun updateLiveLocation(@Body body: ModelUpdateLiveLocationRequest): Response<ModelUpdateLiveLocationResponse>

    @POST(Constant.appStatus)
    suspend fun appStatus(@Body body: ModelAppStatusRequest): Response<ModelAppStatusResponse>

    @POST(Constant.changePassword)
    suspend fun changePassword(@Body body: ModelChangePasswordRequest): Response<ModelChangePasswordResponse>

}