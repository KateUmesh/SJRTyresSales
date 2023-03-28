package com.sjrtyressales.repository

import com.sjrtyressales.model.ModelLoginRequest
import com.sjrtyressales.model.ModelStartMeetingRequest
import com.sjrtyressales.network.ApiInterface
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
    suspend fun submitInTime() = apiInterface.submitInTime()
    suspend fun submitOutTime() = apiInterface.submitOutTime()

    suspend fun uploadProfilePhoto(photo: MultipartBody.Part)=
        apiInterface.uploadProfilePhoto(photo)

    suspend fun postAllowance(photo: MultipartBody.Part,title:RequestBody,amount:RequestBody)=
        apiInterface.postAllowance(photo,title, amount)
}