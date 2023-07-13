package com.sjrtyressales.screens.endMeeting.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.sjrtyressales.repository.RepositoryAPI
import com.sjrtyressales.screens.endMeeting.model.ModelEndMeetingResponse
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
class ViewModelEndMeeting @Inject constructor(val repositoryAPI: RepositoryAPI,
                                              val localSharedPreferences: LocalSharedPreferences,
                                              val networkConnection: NetworkConnection):ViewModel() {

    var mModelEndMeetingResponse = MutableLiveData<ModelEndMeetingResponse>()
    val EndMeetingResponse : LiveData<ModelEndMeetingResponse>
        get() = mModelEndMeetingResponse

    var token = localSharedPreferences.getStringValue(Constant.token)

    fun endMeeting(photo: MultipartBody.Part,
                      id_meeting: RequestBody,
                   distributor_name:RequestBody,
                   distributor_mobile:RequestBody
                      ,meeting_conclusion: RequestBody
                      ,meeting_end_latitude: RequestBody
                      ,meeting_end_longitude: RequestBody){
        if(networkConnection.isNetworkConnected()){
            viewModelScope.launch {
                try {

                    val response = repositoryAPI.endMeeting(photo,id_meeting,distributor_name,distributor_mobile, meeting_conclusion,meeting_end_latitude,meeting_end_longitude)
                    if (response.isSuccessful) {
                        mModelEndMeetingResponse.value = response.body()
                    } else {
                        mModelEndMeetingResponse.value =
                            ModelEndMeetingResponse(
                                "",
                                response.message(),null
                            )
                    }

                } catch (e: Exception) {
                    Log.e("Exception",e.message!!)
                    if (e is SocketTimeoutException)
                        mModelEndMeetingResponse.value =
                            ModelEndMeetingResponse(
                                "",
                                Constant.slow_internet_connection_detected,null
                            )
                    else
                        mModelEndMeetingResponse.value =
                            ModelEndMeetingResponse(
                                "",
                                Constant.something_went_wrong,null
                            )
                }
            }
        }else{
            mModelEndMeetingResponse.value = ModelEndMeetingResponse("",Constant.no_internet_connection,null)
        }

    }


}