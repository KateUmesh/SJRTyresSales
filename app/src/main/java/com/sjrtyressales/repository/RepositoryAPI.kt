package com.sjrtyressales.repository

import com.sjrtyressales.network.ApiInterface
import com.sjrtyressales.screens.changePassword.model.ModelChangePasswordRequest
import com.sjrtyressales.screens.login.model.ModelLoginRequest
import com.sjrtyressales.screens.meetings.model.ModelStartMeetingRequest
import com.sjrtyressales.screens.splashScreen.model.ModelAppStatusRequest
import com.sjrtyressales.screens.splashScreen.model.ModelUpdateLiveLocationRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class RepositoryAPI @Inject constructor(private val apiInterface: ApiInterface) {

    suspend fun postLogin(mModelLoginRequest: ModelLoginRequest) =
        apiInterface.login(mModelLoginRequest)

    suspend fun getDashboard() = apiInterface.getDashBoard()

    suspend fun getMeetingHistory() = apiInterface.getMeetingHistory()

    suspend fun startMeeting(mModelStartMeetingRequest: ModelStartMeetingRequest) =
        apiInterface.startMeeting(mModelStartMeetingRequest)

    suspend fun getAttendance() = apiInterface.getAttendance()
    suspend fun checkMeetingNotEnd() = apiInterface.checkMeetingNotEnd()
    suspend fun submitInTime(latitude:Double,longitude:Double) = apiInterface.submitInTime(latitude,longitude)
    suspend fun submitOutTime(latitude:Double,longitude:Double) = apiInterface.submitOutTime(latitude,longitude)

    suspend fun uploadProfilePhoto(photo: MultipartBody.Part)=
        apiInterface.uploadProfilePhoto(photo)

    suspend fun postAllowance(photo: MultipartBody.Part,title:RequestBody,amount:RequestBody,
                              latitude:RequestBody,longitude:RequestBody)=
        apiInterface.postAllowance(photo,title, amount,latitude, longitude)

    suspend fun viewMeetingDetails(meetingId:Int)=apiInterface.viewMeetingDetails(meetingId)

    suspend fun endMeeting(photo: MultipartBody.Part,
                           id_meeting: RequestBody,
                           distributor_name:RequestBody,
                           distributor_mobile:RequestBody
                           ,meeting_conclusion: RequestBody
                           ,meeting_end_latitude: RequestBody
                           ,meeting_end_longitude: RequestBody)=
        apiInterface.endMeeting(photo,id_meeting,distributor_name,distributor_mobile, meeting_conclusion,meeting_end_latitude,meeting_end_longitude)


    suspend fun myProfile()= apiInterface.myProfile()

    suspend fun updateLiveLocation(mModelUpdateLiveLocationRequest: ModelUpdateLiveLocationRequest) = apiInterface.updateLiveLocation(mModelUpdateLiveLocationRequest)
    suspend fun appStatus(mModelAppStatusRequest: ModelAppStatusRequest) = apiInterface.appStatus(mModelAppStatusRequest)
    suspend fun changePassword(mModelChangePasswordRequest: ModelChangePasswordRequest) = apiInterface.changePassword(mModelChangePasswordRequest)
}