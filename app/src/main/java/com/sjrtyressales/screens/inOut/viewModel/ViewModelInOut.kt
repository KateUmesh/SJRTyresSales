package com.sjrtyressales.screens.inOut.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjrtyressales.screens.inOut.model.ModelAttendanceResponse
import com.sjrtyressales.screens.inOut.model.ModelSubmitInOutTimeResponse
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

    var mModelSubmitOutTimeResponse = MediatorLiveData<ModelSubmitInOutTimeResponse>()
    val SubmitOutTimeResponse : LiveData<ModelSubmitInOutTimeResponse>
        get()= mModelSubmitOutTimeResponse

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

    fun submitInTime(latitude:Double,longitude:Double){
        if(networkConnection.isNetworkConnected()){
            viewModelScope.launch {
                try{
                    val response = repositoryAPI.submitInTime(latitude,longitude)
                    if (response.isSuccessful) {
                        mModelSubmitInOutTimeResponse.value = response.body()
                    } else {
                        mModelSubmitInOutTimeResponse.value = ModelSubmitInOutTimeResponse("2", response.message(),null)
                    }
                }catch(e:Exception){
                    if (e is SocketTimeoutException) {
                        mModelSubmitInOutTimeResponse.value = ModelSubmitInOutTimeResponse(
                            "2",
                            Constant.slow_internet_connection_detected,null
                        )
                    } else {
                        mModelSubmitInOutTimeResponse.value =
                            ModelSubmitInOutTimeResponse("2", Constant.something_went_wrong,null)
                    }
                }

            }

        }else{
            mModelSubmitInOutTimeResponse.value = ModelSubmitInOutTimeResponse("2",Constant.no_internet_connection,null)
        }
    }

    fun submitOutTime(latitude:Double,longitude:Double){
        if(networkConnection.isNetworkConnected()){
            viewModelScope.launch {
                try{
                    val response = repositoryAPI.submitOutTime(latitude,longitude)
                    if (response.isSuccessful) {
                        mModelSubmitOutTimeResponse.value = response.body()
                    } else {
                        mModelSubmitOutTimeResponse.value = ModelSubmitInOutTimeResponse("3", response.message(),null)
                    }
                }catch(e:Exception){
                    if (e is SocketTimeoutException) {
                        mModelSubmitOutTimeResponse.value = ModelSubmitInOutTimeResponse(
                            "3",
                            Constant.slow_internet_connection_detected,null
                        )
                    } else {
                        mModelSubmitOutTimeResponse.value =
                            ModelSubmitInOutTimeResponse("3", Constant.something_went_wrong,null)
                    }
                }

            }

        }else{
            mModelSubmitOutTimeResponse.value = ModelSubmitInOutTimeResponse("3",Constant.no_internet_connection,null)
        }
    }
}