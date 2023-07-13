package com.sjrtyressales.screens.meetingDetails.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjrtyressales.screens.meetingDetails.model.ModelViewMeetingDetailsResponse
import com.sjrtyressales.repository.RepositoryAPI
import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import com.sjrtyressales.utils.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ViewModelMeetingDetails @Inject constructor(
    private val mRepository: RepositoryAPI,
    private val localSharedPreferences: LocalSharedPreferences,
    private val networkConnection: NetworkConnection
) : ViewModel() {

    var mModelViewMeetingDetailsResponse = MutableLiveData<ModelViewMeetingDetailsResponse>()
    val MeetingDetailsResponse :LiveData<ModelViewMeetingDetailsResponse>
    get() = mModelViewMeetingDetailsResponse

    var token = localSharedPreferences.getStringValue(Constant.token)

    fun viewMeetingDetails(meetingId:Int) {
        if(networkConnection.isNetworkConnected()) {
            viewModelScope.launch {
                try {

                    val response = mRepository.viewMeetingDetails(meetingId)
                    if (response.isSuccessful) {
                        mModelViewMeetingDetailsResponse.value = response.body()
                    } else {
                        mModelViewMeetingDetailsResponse.value =
                            ModelViewMeetingDetailsResponse(
                                "",
                                response.message(),null
                            )
                    }

                } catch (e: java.lang.Exception) {
                    if (e is SocketTimeoutException)
                        mModelViewMeetingDetailsResponse.value =
                            ModelViewMeetingDetailsResponse(
                                "",
                                Constant.slow_internet_connection_detected,null
                            )
                    else
                        mModelViewMeetingDetailsResponse.value =
                            ModelViewMeetingDetailsResponse(
                                "",
                                Constant.something_went_wrong,null
                            )
                }
            }
        }else{
            mModelViewMeetingDetailsResponse.value =
                ModelViewMeetingDetailsResponse(
                    "",
                    Constant.no_internet_connection,null
                )
        }

    }
}