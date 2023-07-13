package com.sjrtyressales.screens.dashboard.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjrtyressales.screens.dashboard.model.ModelDashboardResponse
import com.sjrtyressales.repository.RepositoryAPI
import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import com.sjrtyressales.utils.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ViewModelDashboard @Inject constructor(val repositoryAPI: RepositoryAPI,
                                             val localSharedPreferences: LocalSharedPreferences,
                                             val networkConnection: NetworkConnection):ViewModel() {

    var mModelDashboardResponse = MediatorLiveData<ModelDashboardResponse>()
    val dashboardResponse : LiveData<ModelDashboardResponse>
    get()= mModelDashboardResponse

    var token = localSharedPreferences.getStringValue(Constant.token)

    fun getDashboard(){

        if(networkConnection.isNetworkConnected()) {
            viewModelScope.launch {
                try {
                    val response = repositoryAPI.getDashboard()
                    if (response.isSuccessful) {
                        mModelDashboardResponse.value = response.body()
                    } else {
                        mModelDashboardResponse.value = ModelDashboardResponse("", response.message(), null)
                    }

                } catch (e: Exception) {
                    if (e is SocketTimeoutException) {
                        mModelDashboardResponse.value = ModelDashboardResponse(
                            "",
                            Constant.slow_internet_connection_detected,
                            null
                        )
                    } else {
                        mModelDashboardResponse.value =
                            ModelDashboardResponse("", Constant.something_went_wrong, null)
                    }
                }
            }
        }else{
            mModelDashboardResponse.value = ModelDashboardResponse("",Constant.no_internet_connection,null)
        }

    }


}