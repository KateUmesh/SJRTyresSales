package com.sjrtyressales.screens.history.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjrtyressales.screens.history.model.ModelHistoryResponse
import com.sjrtyressales.repository.RepositoryAPI
import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import com.sjrtyressales.utils.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ViewModelHistory @Inject constructor(val repositoryAPI: RepositoryAPI,
                                           val localSharedPreferences: LocalSharedPreferences,
                                           val networkConnection: NetworkConnection):ViewModel() {

    var mModelHistoryResponse = MediatorLiveData<ModelHistoryResponse>()
    val HistoryResponse : LiveData<ModelHistoryResponse>
    get()= mModelHistoryResponse

    var token = localSharedPreferences.getStringValue(Constant.token)

    fun getHistory(){

        if(networkConnection.isNetworkConnected()) {
            viewModelScope.launch {
                try {
                    val response = repositoryAPI.getMeetingHistory()
                    if (response.isSuccessful) {
                        mModelHistoryResponse.value = response.body()
                    } else {
                        mModelHistoryResponse.value = ModelHistoryResponse("", response.message(), null)
                    }

                } catch (e: Exception) {
                    if (e is SocketTimeoutException) {
                        mModelHistoryResponse.value = ModelHistoryResponse(
                            "",
                            Constant.slow_internet_connection_detected,
                            null
                        )
                    } else {
                        mModelHistoryResponse.value =
                            ModelHistoryResponse("", Constant.something_went_wrong, null)
                    }
                }
            }
        }else{
            mModelHistoryResponse.value = ModelHistoryResponse("",Constant.no_internet_connection,null)
        }

    }


}