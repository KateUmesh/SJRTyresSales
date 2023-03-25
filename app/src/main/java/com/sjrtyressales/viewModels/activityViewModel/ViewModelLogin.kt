package com.sjrtyressales.viewModels.activityViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjrtyressales.model.ModelLoginRequest
import com.sjrtyressales.model.ModelLoginResponse
import com.sjrtyressales.repository.RepositoryAPI
import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import com.sjrtyressales.utils.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ViewModelLogin @Inject constructor(val repositoryAPI: RepositoryAPI,
                                         val localSharedPreferences: LocalSharedPreferences,
                                         val networkConnection: NetworkConnection):ViewModel() {

    var mModelLoginResponse = MediatorLiveData<ModelLoginResponse>()
    val loginResponse : LiveData<ModelLoginResponse>
    get()= mModelLoginResponse

    fun postLogin(mModelLoginRequest: ModelLoginRequest){

        if(networkConnection.isNetworkConnected()) {
            viewModelScope.launch {
                try {
                    val response = repositoryAPI.postLogin(mModelLoginRequest)
                    if (response.isSuccessful) {
                        mModelLoginResponse.value = response.body()
                    } else {
                        mModelLoginResponse.value = ModelLoginResponse("", response.message(), null)
                    }

                } catch (e: Exception) {
                    if (e is SocketTimeoutException) {
                        mModelLoginResponse.value = ModelLoginResponse(
                            "",
                            Constant.slow_internet_connection_detected,
                            null
                        )
                    } else {
                        mModelLoginResponse.value =
                            ModelLoginResponse("", Constant.something_went_wrong, null)
                    }
                }
            }
        }else{
            mModelLoginResponse.value = ModelLoginResponse("",Constant.no_internet_connection,null)
        }

    }

    fun putToken(value:String){
        localSharedPreferences.putStringValue(Constant.token,value)
    }

}