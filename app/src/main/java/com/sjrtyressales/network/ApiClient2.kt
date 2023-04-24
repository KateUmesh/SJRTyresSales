package com.sjrtyressales.network

import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

object ApiClient2 {



    private var servicesApiInterface: ApiInterface1? = null


    /**
     * This method is initialize for retrofit object
     * @return ApiInterface instance
     */


    init {
        System.loadLibrary("native-lib")
    }


    @Singleton
    fun getUnsafeOkHttpClient(token:String): OkHttpClient.Builder {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        //.addInterceptor(interceptor)

        val builder = OkHttpClient.Builder()
        builder
            .addInterceptor(interceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("x-api-key",Constant.KEY)
                    .addHeader("Authorization", Constant.Bearer+ token)
                    .build()
                chain.proceed(newRequest)
            }
        return builder
    }
    fun build(token:String): ApiInterface1? {
        val builder: Retrofit.Builder = Retrofit.Builder()
            .baseUrl(Constant.BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())

        val httpClient: OkHttpClient.Builder=
            getUnsafeOkHttpClient(token)

        val retrofit: Retrofit = builder.client(httpClient.build()).build()
        servicesApiInterface = retrofit.create(
            ApiInterface1::class.java
        )

        return servicesApiInterface as ApiInterface1
    }
}