package com.example.minorfinal.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// --- 1. Data classes to match OpenWeatherMap's JSON ---
data class WeatherResponse(
    val main: Main,
    val name: String // <-- ADDED THIS LINE to get the city name
)
data class Main(
    val temp: Double,
    val humidity: Int
)

// --- 2. The API Service Interface ---
interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // Gets temp in Celsius
    ): Response<WeatherResponse>
}

// --- 3. A separate Retrofit Client for the Weather API ---
object WeatherRetrofitClient {
    private const val WEATHER_BASE_URL = "https://api.openweathermap.org/"

    val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}