package com.sjrtyressales.repository

import com.sjrtyressales.model.ModelLoginRequest
import com.sjrtyressales.network.ApiInterface
import javax.inject.Inject

class RepositoryAPI @Inject constructor(private val apiInterface: ApiInterface) {

    suspend fun postLogin(mModelLoginRequest: ModelLoginRequest) = apiInterface.login(mModelLoginRequest)

    suspend fun getDashboard() = apiInterface.getDashBoard()
}