package com.example.minorfinal

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// Import your network files
import com.example.minorfinal.network.ApiService
import com.example.minorfinal.network.RetrofitClient
import com.example.minorfinal.network.ShelfLifeRequest
import com.example.minorfinal.network.WeatherApiService // NEW IMPORT
import com.example.minorfinal.network.WeatherRetrofitClient // NEW IMPORT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 1. UPDATE THE UI STATE
// Add fields to store the fetched weather
data class ClassifierUiState(
    val isLoadingModel: Boolean = false,
    val isLoadingNetwork: Boolean = false,
    val imageUri: Uri? = null,
    val classificationResults: List<ClassificationResult> = emptyList(),
    val selectedFood: ClassificationResult? = null,
    val predictionResult: String? = null,
    val errorMessage: String? = null,

    // Storage options
    val storageOptions: List<String> = listOf("Refrigerated", "Open", "Airtight"),
    val storageInput: String = "Refrigerated",

    // --- NEW FIELDS to store weather ---
    val fetchedCity: String? = null,
    val fetchedTemp: Double? = null,
    val fetchedHumidity: Int? = null
)

class ClassifierViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ClassifierUiState())
    val uiState: StateFlow<ClassifierUiState> = _uiState.asStateFlow()

    private lateinit var classifier: FoodClassifier

    private val apiService: ApiService = RetrofitClient.api
    private val weatherApiService: WeatherApiService = WeatherRetrofitClient.api

    // --- YOUR API KEY ---
    private val WEATHER_API_KEY = "705983e7d128e5c3a000d2213bd0b779"

    // --- (initClassifier and onImageSelected are unchanged) ---
    private fun initClassifier() {
        if (!::classifier.isInitialized) {
            try {
                classifier = FoodClassifier(getApplication())
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to initialize classifier: ${e.message}") }
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        _uiState.update {
            it.copy(
                imageUri = uri,
                isLoadingModel = true,
                classificationResults = emptyList(),
                selectedFood = null,
                predictionResult = null,
                errorMessage = null
            )
        }
        runClassification(uri)
    }


    // --- (onFoodSelected and onStorageChanged are unchanged) ---
    fun onFoodSelected(result: ClassificationResult) {
        _uiState.update {
            it.copy(
                selectedFood = result,
                predictionResult = null,
                errorMessage = null
            )
        }
    }

    fun onStorageChanged(storage: String) {
        _uiState.update { it.copy(storageInput = storage) }
    }

    // --- UPDATED getPrediction FUNCTION ---
    fun getPrediction(latitude: Double, longitude: Double) {
        val dishName = _uiState.value.selectedFood?.label ?: return
        val storage = _uiState.value.storageInput

        _uiState.update {
            it.copy(
                isLoadingNetwork = true,
                errorMessage = null,
                predictionResult = null,
                // Clear old weather data
                fetchedCity = null,
                fetchedTemp = null,
                fetchedHumidity = null
            )
        }

        viewModelScope.launch {
            try {
                // --- CALL 1: Get Weather ---
                val weatherResponse = weatherApiService.getCurrentWeather(
                    latitude = latitude,
                    longitude = longitude,
                    apiKey = WEATHER_API_KEY,
                    units = "metric"
                )

                if (!weatherResponse.isSuccessful || weatherResponse.body() == null) {
                    throw Exception("Weather API Error: ${weatherResponse.message()}")
                }

                // Get data from weather response
                val weatherData = weatherResponse.body()!!
                val temp = weatherData.main.temp
                val humidity = weatherData.main.humidity.toDouble()
                val city = weatherData.name

                // --- !!! NEW: SAVE WEATHER TO STATE !!! ---
                _uiState.update {
                    it.copy(
                        fetchedCity = city,
                        fetchedTemp = temp,
                        fetchedHumidity = weatherData.main.humidity
                    )
                }

                // --- CALL 2: Get Shelf Life ---
                val request = ShelfLifeRequest(
                    dish_name = dishName,
                    temperature = temp,
                    humidity = humidity,
                    storage = storage
                )

                val shelfLifeResponse = apiService.predictShelfLife(request)

                if (!shelfLifeResponse.isSuccessful || shelfLifeResponse.body() == null) {
                    throw Exception("Shelf Life API Error: ${shelfLifeResponse.message()}")
                }

                // SUCCESS!
                _uiState.update {
                    it.copy(
                        isLoadingNetwork = false,
                        predictionResult = shelfLifeResponse.body()!!.formatted
                    )
                }

            } catch (e: Exception) {
                // Handle any error from either API call
                _uiState.update {
                    it.copy(isLoadingNetwork = false, errorMessage = "Network Error: ${e.message}")
                }
            }
        }
    }

    // --- UPDATED clearSelection FUNCTION ---
    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedFood = null,
                predictionResult = null,
                errorMessage = null,
                isLoadingNetwork = false,
                // --- Clear weather data ---
                fetchedCity = null,
                fetchedTemp = null,
                fetchedHumidity = null
            )
        }
    }

    // --- (runClassification and loadBitmapFromUri are unchanged) ---
    private fun runClassification(uri: Uri) {
        viewModelScope.launch {
            initClassifier()
            if (!::classifier.isInitialized) {
                _uiState.update { it.copy(isLoadingModel = false) }
                return@launch
            }

            val bitmap = loadBitmapFromUri(uri)
            if (bitmap == null) {
                _uiState.update { it.copy(errorMessage = "Failed to load image.", isLoadingModel = false) }
                return@launch
            }

            val results = withContext(Dispatchers.Default) {
                classifier.classify(bitmap)
            }

            _uiState.update {
                it.copy(
                    classificationResults = results,
                    isLoadingModel = false
                )
            }
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        val context = getApplication<Application>().applicationContext
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Error loading bitmap: ${e.message}") }
            Log.e("ClassifierViewModel", "Error loading bitmap", e)
            null
        }
    }
}