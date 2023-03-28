package com.sjrtyressales.viewModels.activityViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjrtyressales.model.ModelStartMeetingRequest
import com.sjrtyressales.model.ModelStartMeetingResponse
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

    var token = localSharedPreferences.getStringValue(Constant.token)

    fun postStartMeeting(mModelStartMeetingRequest: ModelStartMeetingRequest){

        if(networkConnection.isNetworkConnected()) {
            viewModelScope.launch {
                try {
                    val response = repositoryAPI.startMeeting(mModelStartMeetingRequest)
                    if (response.isSuccessful) {
                        mModelStartMeetingResponse.value = response.body()
                    } else {
                        mModelStartMeetingResponse.value = ModelStartMeetingResponse("", response.message())
                    }

                } catch (e: Exception) {
                    if (e is SocketTimeoutException) {
                        mModelStartMeetingResponse.value = ModelStartMeetingResponse(
                            "",
                            Constant.slow_internet_connection_detected
                        )
                    } else {
                        mModelStartMeetingResponse.value =
                            ModelStartMeetingResponse("", Constant.something_went_wrong)
                    }
                }
            }
        }else{
            mModelStartMeetingResponse.value = ModelStartMeetingResponse("",Constant.no_internet_connection)
        }

    }

    fun putToken(value:String){
        localSharedPreferences.putStringValue(Constant.token,value)
    }

}