package com.example.minorfinal.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// 1. API Interface
interface ApiService {
    // This MUST match your server.py @app.post("/predict")
    @POST("predict")
    suspend fun predictShelfLife(
        @Body request: ShelfLifeRequest
    ): Response<ShelfLifeResponse>
}

// 2. Request Data Class (Must match server's PredictRequest)
data class ShelfLifeRequest(
    val dish_name: String,
    val temperature: Double,
    val humidity: Double,
    val storage: String
)

// 3. Response Data Class (Must match what your server returns)
data class ShelfLifeResponse(
    val raw_hours: Double,
    val adjusted_hours: Double,
    val formatted: String // This is the "10 hours 5 minutes"
)