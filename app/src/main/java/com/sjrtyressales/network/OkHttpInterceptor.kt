package com.sjrtyressales.network

import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class OkHttpInterceptor @Inject constructor(private val localSharedPreferences: LocalSharedPreferences):Interceptor{
    /*init {
        System.loadLibrary("native-lib")
    }*/

    external fun key():String
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        request.addHeader("x-api-key","9732a6ea-75d3-4a30-9309-ae1de96d9014")
        request.addHeader("Authorization",Constant.Bearer+localSharedPreferences.getStringValue(Constant.token))
        return chain.proceed(request.build())
    }


}