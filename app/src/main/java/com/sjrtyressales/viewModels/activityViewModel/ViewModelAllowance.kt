package com.sjrtyressales.viewModels.activityViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjrtyressales.model.ModelAllowanceResponse
import com.sjrtyressales.model.ModelProfilePhotoResponse
import com.sjrtyressales.repository.RepositoryAPI
import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import com.sjrtyressales.utils.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ViewModelAllowance @Inject constructor(
    private val mRepository: RepositoryAPI,
    private val localSharedPreferences: LocalSharedPreferences,
    private val networkConnection: NetworkConnection
) : ViewModel(){

    var mModelAllowanceResponse = MutableLiveData<ModelAllowanceResponse>()
    val AllowanceResponse : LiveData<ModelAllowanceResponse>
        get() = mModelAllowanceResponse

    var token = localSharedPreferences.getStringValue(Constant.token)

    fun postAllowance(photo: MultipartBody.Part, title: RequestBody, amount: RequestBody,latitude:RequestBody,longitude:RequestBody){
        if(networkConnection.isNetworkConnected()){
            viewModelScope.launch {
                try {

                    val response = mRepository.postAllowance(photo,title, amount,latitude, longitude)
                    if (response?.isSuccessful!!) {
                        mModelAllowanceResponse.value = response.body()
                    } else {
                        mModelAllowanceResponse.value =
                            ModelAllowanceResponse(
                                "",
                                response.message(),null
                            )
                    }

                } catch (e: Exception) {
                    Log.e("Exception",e.message!!)
                    if (e is SocketTimeoutException)
                        mModelAllowanceResponse.value =
                            ModelAllowanceResponse(
                                "",
                                Constant.slow_internet_connection_detected,null
                            )
                    else
                        mModelAllowanceResponse.value =
                            ModelAllowanceResponse(
                                "",
                                Constant.something_went_wrong,null
                            )
                }
            }
        }else{
            mModelAllowanceResponse.value = ModelAllowanceResponse("",Constant.no_internet_connection,null)
        }

    }

}
