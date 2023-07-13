package com.sjrtyressales.screens.changePassword.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjrtyressales.screens.changePassword.model.ModelChangePasswordRequest
import com.sjrtyressales.screens.changePassword.model.ModelChangePasswordResponse
import com.sjrtyressales.repository.RepositoryAPI
import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import com.sjrtyressales.utils.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ViewModelChangePassword @Inject constructor(val repositoryAPI: RepositoryAPI,
                                                  val localSharedPreferences: LocalSharedPreferences,
                                                  val networkConnection: NetworkConnection):ViewModel() {

    var mModelChangePasswordResponse = MediatorLiveData<ModelChangePasswordResponse>()
    val ChangePasswordResponse : LiveData<ModelChangePasswordResponse>
    get()= mModelChangePasswordResponse

    var token = localSharedPreferences.getStringValue(Constant.token)

    fun changePassword(mModelChangePasswordRequest: ModelChangePasswordRequest){

        if(networkConnection.isNetworkConnected()) {
            viewModelScope.launch {
                try {
                    val response = repositoryAPI.changePassword(mModelChangePasswordRequest)
                    if (response.isSuccessful) {
                        mModelChangePasswordResponse.value = response.body()
                    } else {
                        mModelChangePasswordResponse.value = ModelChangePasswordResponse("", response.message(), null)
                    }

                } catch (e: Exception) {
                    if (e is SocketTimeoutException) {
                        mModelChangePasswordResponse.value = ModelChangePasswordResponse(
                            "",
                            Constant.slow_internet_connection_detected,
                            null
                        )
                    } else {
                        mModelChangePasswordResponse.value =
                            ModelChangePasswordResponse("", Constant.something_went_wrong, null)
                    }
                }
            }
        }else{
            mModelChangePasswordResponse.value = ModelChangePasswordResponse("",Constant.no_internet_connection,null)
        }

    }


}