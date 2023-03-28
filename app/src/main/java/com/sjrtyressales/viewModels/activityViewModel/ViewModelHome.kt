package com.sjrtyressales.viewModels.activityViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjrtyressales.model.ModelProfilePhotoResponse
import com.sjrtyressales.repository.RepositoryAPI
import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import com.sjrtyressales.utils.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ViewModelHome @Inject constructor(
    private val mRepository: RepositoryAPI,
    private val localSharedPreferences: LocalSharedPreferences,
    private val networkConnection: NetworkConnection
) : ViewModel() {

    var mModelProfilePhotoResponse = MutableLiveData<ModelProfilePhotoResponse>()
    val profilePhotoResponse :LiveData<ModelProfilePhotoResponse>
    get() = mModelProfilePhotoResponse

    var token = localSharedPreferences.getStringValue(Constant.token)

    fun uploadProfilePhoto(photo: MultipartBody.Part) {
        if(networkConnection.isNetworkConnected()) {
            viewModelScope.launch {
                try {

                    val response = mRepository.uploadProfilePhoto(photo)
                    if (response?.isSuccessful!!) {
                        mModelProfilePhotoResponse.value = response.body()
                    } else {
                        mModelProfilePhotoResponse.value =
                            ModelProfilePhotoResponse(
                                "",
                                response.message()
                            )
                    }

                } catch (e: java.lang.Exception) {
                    if (e is SocketTimeoutException)
                        mModelProfilePhotoResponse.value =
                            ModelProfilePhotoResponse(
                                "",
                                Constant.slow_internet_connection_detected
                            )
                    else
                        mModelProfilePhotoResponse.value =
                            ModelProfilePhotoResponse(
                                "",
                                Constant.something_went_wrong
                            )
                }
            }
        }else{
            mModelProfilePhotoResponse.value =
                ModelProfilePhotoResponse(
                    "",
                    Constant.no_internet_connection
                )
        }

    }
}