package com.sjrtyressales.screens.meetings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjrtyressales.screens.meetings.model.ModelCheckMeetingNotEndResponse
import com.sjrtyressales.screens.meetings.model.ModelStartMeetingRequest
import com.sjrtyressales.screens.meetings.model.ModelStartMeetingResponse
import com.sjrtyressales.repository.RepositoryAPI
import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import com.sjrtyressales.utils.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ViewModelMeetings @Inject constructor(val repositoryAPI: RepositoryAPI,
                                            val localSharedPreferences: LocalSharedPreferences,
                                            val networkConnection: NetworkConnection):ViewModel() {

    var mModelStartMeetingResponse = MediatorLiveData<ModelStartMeetingResponse>()
    val StartMeetingResponse : LiveData<ModelStartMeetingResponse>
    get()= mModelStartMeetingResponse

    var mModelCheckMeetingNotEndResponse = MediatorLiveData<ModelCheckMeetingNotEndResponse>()
    val CheckMeetingNotEndResponse : LiveData<ModelCheckMeetingNotEndResponse>
        get()= mModelCheckMeetingNotEndResponse

    var token = localSharedPreferences.getStringValue(Constant.token)

    fun postStartMeeting(mModelStartMeetingRequest: ModelStartMeetingRequest){

        if(networkConnection.isNetworkConnected()) {
            viewModelScope.launch {
                try {
                    val response = repositoryAPI.startMeeting(mModelStartMeetingRequest)
                    if (response.isSuccessful) {
                        mModelStartMeetingResponse.value = response.body()
                    } else {
                        mModelStartMeetingResponse.value = ModelStartMeetingResponse("2", response.message(),null)
                    }

                } catch (e: Exception) {
                    if (e is SocketTimeoutException) {
                        mModelStartMeetingResponse.value = ModelStartMeetingResponse(
                            "2",
                            Constant.slow_internet_connection_detected,null
                        )
                    } else {
                        mModelStartMeetingResponse.value =
                            ModelStartMeetingResponse("2", Constant.something_went_wrong,null)
                    }
                }
            }
        }else{
            mModelStartMeetingResponse.value = ModelStartMeetingResponse("2",Constant.no_internet_connection,null)
        }

    }

    fun checkMeetingNotEnd(){
        if(networkConnection.isNetworkConnected()) {
            viewModelScope.launch {
                try {
                    val response = repositoryAPI.checkMeetingNotEnd()
                    if (response.isSuccessful) {
                        mModelCheckMeetingNotEndResponse.value = response.body()
                    } else {
                        mModelCheckMeetingNotEndResponse.value = ModelCheckMeetingNotEndResponse("", response.message(),null)
                    }

                } catch (e: Exception) {
                    if (e is SocketTimeoutException) {
                        mModelCheckMeetingNotEndResponse.value = ModelCheckMeetingNotEndResponse(
                            "",
                            Constant.slow_internet_connection_detected,null
                        )
                    } else {
                        mModelCheckMeetingNotEndResponse.value =
                            ModelCheckMeetingNotEndResponse("", Constant.something_went_wrong,null)
                    }
                }
            }
        }else{
            mModelCheckMeetingNotEndResponse.value = ModelCheckMeetingNotEndResponse("",Constant.no_internet_connection,null)
        }
    }

}