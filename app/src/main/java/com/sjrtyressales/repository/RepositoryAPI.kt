package com.sjrtyressales.repository

import com.sjrtyressales.model.ModelLoginRequest
import com.sjrtyressales.model.ModelStartMeetingRequest
import com.sjrtyressales.network.ApiInterface
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Path
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

    suspend fun postAllowance(photo: MultipartBody.Part,title:RequestBody,amount:RequestBody)=
        apiInterface.postAllowance(photo,title, amount)

    suspend fun viewMeetingDetails(meetingId:Int)=apiInterface.viewMeetingDetails(meetingId)
}