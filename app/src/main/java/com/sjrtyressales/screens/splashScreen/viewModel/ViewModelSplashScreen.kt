package com.sjrtyressales.screens.splashScreen.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.sjrtyressales.repository.RepositoryAPI
import com.sjrtyressales.screens.splashScreen.model.ModelAppStatusRequest
import com.sjrtyressales.screens.splashScreen.model.ModelAppStatusResponse
import com.sjrtyressales.screens.splashScreen.model.ModelUpdateLiveLocationRequest
import com.sjrtyressales.screens.splashScreen.model.ModelUpdateLiveLocationResponse
import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import com.sjrtyressales.utils.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ViewModelSplashScreen @Inject constructor(
    private val mRepository: RepositoryAPI,
    private val localSharedPreferences: LocalSharedPreferences,
    private val networkConnection: NetworkConnection
) : ViewModel(){

    var mModelUpdateLiveLocationResponse = MutableLiveData<ModelUpdateLiveLocationResponse>()
    val UpdateLiveLocationResponse : LiveData<ModelUpdateLiveLocationResponse>
        get() = mModelUpdateLiveLocationResponse

    var mModelAppStatusResponse = MutableLiveData<ModelAppStatusResponse>()
    val AppStatusResponse : LiveData<ModelAppStatusResponse>
        get() = mModelAppStatusResponse



    var token = localSharedPreferences.getStringValue(Constant.token)

    fun updateLiveLocation(mModelUpdateLiveLocationRequest: ModelUpdateLiveLocationRequest){
        if(networkConnection.isNetworkConnected()){
            viewModelScope.launch {
                try {

                    val response = mRepository.updateLiveLocation(mModelUpdateLiveLocationRequest)
                    if (response.isSuccessful) {
                        mModelUpdateLiveLocationResponse.value = response.body()
                    } else {
                        mModelUpdateLiveLocationResponse.value =
                            ModelUpdateLiveLocationResponse(
                                "",
                                response.message(),null
                            )
                    }

                } catch (e: Exception) {
                    Log.e("Exception",e.message!!)
                    if (e is SocketTimeoutException)
                        mModelUpdateLiveLocationResponse.value =
                            ModelUpdateLiveLocationResponse(
                                "",
                                Constant.slow_internet_connection_detected,null
                            )
                    else
                        mModelUpdateLiveLocationResponse.value =
                            ModelUpdateLiveLocationResponse(
                                "",
                                Constant.something_went_wrong,null
                            )
                }
            }
        }else{
            mModelUpdateLiveLocationResponse.value = ModelUpdateLiveLocationResponse("",Constant.no_internet_connection,null)
        }

    }

    fun appStatus(mModelUpdateLiveLocationRequest: ModelAppStatusRequest){
        if(networkConnection.isNetworkConnected()){
            viewModelScope.launch {
                try {

                    val response = mRepository.appStatus(mModelUpdateLiveLocationRequest)
                    if (response.isSuccessful) {
                        mModelAppStatusResponse.value = response.body()
                    } else {
                        mModelAppStatusResponse.value =
                            ModelAppStatusResponse(
                                "",
                                response.message(),null
                            )
                    }

                } catch (e: Exception) {
                    Log.e("Exception",e.message!!)
                    if (e is SocketTimeoutException)
                        mModelAppStatusResponse.value =
                            ModelAppStatusResponse(
                                "",
                                Constant.slow_internet_connection_detected,null
                            )
                    else
                        mModelAppStatusResponse.value =
                            ModelAppStatusResponse(
                                "",
                                Constant.something_went_wrong,null
                            )
                }
            }
        }else{
            mModelAppStatusResponse.value = ModelAppStatusResponse("",Constant.no_internet_connection,null)
        }

    }



}
