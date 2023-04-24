package com.sjrtyressales.network

import com.sjrtyressales.utils.Constant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiClient {

    /*init {
        System.loadLibrary("native_lib")
    }

    external fun baseUrl():String*/

    @Singleton
    @Provides
    fun provideRetrofitBuilder():Retrofit.Builder{
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(Constant.BASE_URL)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(okHttpInterceptor: OkHttpInterceptor):OkHttpClient{
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(okHttpInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideApiInterface(retrofitBuilder:Retrofit.Builder,okHttpClient: OkHttpClient):ApiInterface{
        return retrofitBuilder.client(okHttpClient).build().create(ApiInterface::class.java)
    }


}