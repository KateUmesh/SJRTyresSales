package com.sjrtyressales.viewModels.activityViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjrtyressales.model.ModelAttendanceResponse
import com.sjrtyressales.model.ModelHistoryResponse
import com.sjrtyressales.model.ModelSubmitInOutTimeResponse
import com.sjrtyressales.repository.RepositoryAPI
import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import com.sjrtyressales.utils.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ViewModelInOut @Inject constructor(
    val repositoryAPI: RepositoryAPI,
    val localSharedPreferences: LocalSharedPreferences,
    val networkConnection: NetworkConnection
) : ViewModel() {

    var mModelAttendanceResponse = MediatorLiveData<ModelAttendanceResponse>()
    val AttendanceResponse : LiveData<ModelAttendanceResponse>
        get()= mModelAttendanceResponse

    var mModelSubmitInOutTimeResponse = MediatorLiveData<ModelSubmitInOutTimeResponse>()
    val SubmitInOutTimeResponse : LiveData<ModelSubmitInOutTimeResponse>
        get()= mModelSubmitInOutTimeResponse

    var token = localSharedPreferences.getStringValue(Constant.token)

    fun getAttendance(){
        if(networkConnection.isNetworkConnected()){
            viewModelScope.launch {
                try{
                    val response = repositoryAPI.getAttendance()
                    if (response.isSuccessful) {
                        mModelAttendanceResponse.value = response.body()
                    } else {
                        mModelAttendanceResponse.value = ModelAttendanceResponse("", response.message(), null)
                    }
                }catch(e:Exception){
                    if (e is SocketTimeoutException) {
                        mModelAttendanceResponse.value = ModelAttendanceResponse(
                            "",
                            Constant.slow_internet_connection_detected,
                            null
                        )
                    } else {
                        mModelAttendanceResponse.value =
                            ModelAttendanceResponse("", Constant.something_went_wrong, null)
                    }
                }

            }

        }else{
            mModelAttendanceResponse.value = ModelAttendanceResponse("",Constant.no_internet_connection,null)
        }
    }

    fun submitInTime(){
        if(networkConnection.isNetworkConnected()){
            viewModelScope.launch {
                try{
                    val response = repositoryAPI.submitInTime()
                    if (response.isSuccessful) {
                        mModelSubmitInOutTimeResponse.value = response.body()
                    } else {
                        mModelSubmitInOutTimeResponse.value = ModelSubmitInOutTimeResponse("", response.message())
                    }
                }catch(e:Exception){
                    if (e is SocketTimeoutException) {
                        mModelSubmitInOutTimeResponse.value = ModelSubmitInOutTimeResponse(
                            "",
                            Constant.slow_internet_connection_detected
                        )
                    } else {
                        mModelSubmitInOutTimeResponse.value =
                            ModelSubmitInOutTimeResponse("", Constant.something_went_wrong)
                    }
                }

            }

        }else{
            mModelSubmitInOutTimeResponse.value = ModelSubmitInOutTimeResponse("",Constant.no_internet_connection)
        }
    }

    fun submitOutTime(){
        if(networkConnection.isNetworkConnected()){
            viewModelScope.launch {
                try{
                    val response = repositoryAPI.submitOutTime()
                    if (response.isSuccessful) {
                        mModelSubmitInOutTimeResponse.value = response.body()
                    } else {
                        mModelSubmitInOutTimeResponse.value = ModelSubmitInOutTimeResponse("", response.message())
                    }
                }catch(e:Exception){
                    if (e is SocketTimeoutException) {
                        mModelSubmitInOutTimeResponse.value = ModelSubmitInOutTimeResponse(
                            "",
                            Constant.slow_internet_connection_detected
                        )
                    } else {
                        mModelSubmitInOutTimeResponse.value =
                            ModelSubmitInOutTimeResponse("", Constant.something_went_wrong)
                    }
                }

            }

        }else{
            mModelSubmitInOutTimeResponse.value = ModelSubmitInOutTimeResponse("",Constant.no_internet_connection)
        }
    }
}