package com.sjrtyressales.network

import com.sjrtyressales.utils.Constant
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


public class ApiClient1 {

    companion object{

        private var retrofit: Retrofit? = null

        var httpClient: OkHttpClient.Builder = OkHttpClient.Builder()




        public fun getClient(): Retrofit? {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(Constant.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
            }
            return retrofit
        }

    }







}