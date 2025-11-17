package com.example.minorfinal.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // --- IMPORTANT: SET YOUR SERVER URL ---
    // Use this if running on the Android Emulator
    private const val BASE_URL = "http://10.207.136.100:8000/"

    // Use this if running on a REAL Phone (replace with your PC's IP)
    // private const val BASE_URL = "http://192.168.1.10:8000/"
    // ------------------------------------

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}