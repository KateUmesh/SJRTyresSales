package com.sjrtyressales.viewModels.activityViewModel

import android.util.Log
import androidx.lifecycle.*
import com.sjrtyressales.model.*
import com.sjrtyressales.repository.RepositoryAPI
import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import com.sjrtyressales.utils.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.lang.invoke.ConstantCallSite
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

    var mModelMyProfileResponse = MutableLiveData<ModelMyProfileResponse>()
    val MyProfileResponse :LiveData<ModelMyProfileResponse>
        get() = mModelMyProfileResponse

    var mModelSubmitInOutTimeResponse = MediatorLiveData<ModelSubmitInOutTimeResponse>()
    val SubmitInOutTimeResponse : LiveData<ModelSubmitInOutTimeResponse>
        get()= mModelSubmitInOutTimeResponse

    var mModelUpdateLiveLocationResponse = MutableLiveData<ModelUpdateLiveLocationResponse>()
    val UpdateLiveLocationResponse : LiveData<ModelUpdateLiveLocationResponse>
        get() = mModelUpdateLiveLocationResponse

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
                                response.message(),null
                            )
                    }

                } catch (e: java.lang.Exception) {
                    if (e is SocketTimeoutException)
                        mModelProfilePhotoResponse.value =
                            ModelProfilePhotoResponse(
                                "",
                                Constant.slow_internet_connection_detected,null
                            )
                    else
                        mModelProfilePhotoResponse.value =
                            ModelProfilePhotoResponse(
                                "",
                                Constant.something_went_wrong,null
                            )
                }
            }
        }else{
            mModelProfilePhotoResponse.value =
                ModelProfilePhotoResponse(
                    "",
                    Constant.no_internet_connection,null
                )
        }

    }

    fun myProfile(){
        if(networkConnection.isNetworkConnected()){
            viewModelScope.launch {
                try {

                    val response = mRepository.myProfile()
                    if (response.isSuccessful) {
                        mModelMyProfileResponse.value = response.body()
                    } else {
                        mModelMyProfileResponse.value =
                            ModelMyProfileResponse(
                                "2",
                                response.message(),null
                            )
                    }

                } catch (e: java.lang.Exception) {
                    if (e is SocketTimeoutException)
                        mModelMyProfileResponse.value =
                            ModelMyProfileResponse(
                                "2",
                                Constant.slow_internet_connection_detected,null
                            )
                    else
                        mModelMyProfileResponse.value =
                            ModelMyProfileResponse(
                                "2",
                                Constant.something_went_wrong,null
                            )
                }
            }
        }else{
            mModelMyProfileResponse.value = ModelMyProfileResponse("2",Constant.no_internet_connection,null)
        }
    }

    fun submitInTime(latitude:Double,longitude:Double){
        if(networkConnection.isNetworkConnected()){
            viewModelScope.launch {
                try{
                    val response = mRepository.submitInTime(latitude,longitude)
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
}