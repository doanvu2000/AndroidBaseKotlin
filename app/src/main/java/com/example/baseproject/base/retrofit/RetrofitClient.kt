package com.example.baseproject.base.retrofit

import com.example.baseproject.base.utils.Constant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val BASE_URL = "https://638088828efcfcedac07840a.mockapi.io"
    var INSTANCE: Retrofit? = null
    fun getInstance(): Retrofit =
        INSTANCE ?: synchronized(this) {
//            INSTANCE = retrofitBuilder()
//            return INSTANCE!!
            INSTANCE ?: retrofitBuilder().also {
                INSTANCE = it
            }
        }

    private fun retrofitBuilder(): Retrofit {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(Constant.READ_TIME_OUT, TimeUnit.SECONDS)
            .connectTimeout(Constant.CONNECT_TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(interceptor).build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
}