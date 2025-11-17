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
// Import your new network files
import com.example.minorfinal.network.ApiService
import com.example.minorfinal.network.RetrofitClient
import com.example.minorfinal.network.ShelfLifeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 1. Define a UI State to hold all screen data
data class ClassifierUiState(
    val isLoadingModel: Boolean = false, // For TFLite model
    val isLoadingNetwork: Boolean = false, // For Server call
    val imageUri: Uri? = null,
    val classificationResults: List<ClassificationResult> = emptyList(), // From Model 1
    val selectedFood: ClassificationResult? = null, // The selected food item
    val predictionResult: String? = null, // From Model 2 (Server) "10 hours..."
    val errorMessage: String? = null
)

class ClassifierViewModel(application: Application) : AndroidViewModel(application) {

    // 2. Use a single StateFlow for the UI State
    private val _uiState = MutableStateFlow(ClassifierUiState())
    val uiState: StateFlow<ClassifierUiState> = _uiState.asStateFlow()

    // 3. Add Classifier and API Service
    private lateinit var classifier: FoodClassifier
    private val apiService: ApiService = RetrofitClient.api

    // --- Public Functions (Called by the UI) ---

    private fun initClassifier() {
        if (!::classifier.isInitialized) {
            try {
                classifier = FoodClassifier(getApplication())
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to initialize classifier: ${e.message}") }
            }
        }
    }

    /**
     * Called when an image is picked from gallery or taken with camera.
     * Runs the local TFLite model (Model 1).
     */
    fun onImageSelected(uri: Uri) {
        _uiState.update {
            it.copy(
                imageUri = uri,
                isLoadingModel = true, // Start Model 1 loading
                classificationResults = emptyList(),
                selectedFood = null,
                predictionResult = null,
                errorMessage = null
            )
        }
        runClassification(uri)
    }

    /**
     * Called when the user taps one of the 3 result buttons.
     * Runs the network call to the Python server (Model 2).
     */
    fun onFoodSelected(result: ClassificationResult) {
        _uiState.update {
            it.copy(
                isLoadingNetwork = true, // Start Model 2 loading
                selectedFood = result,
                predictionResult = null,
                errorMessage = null
            )
        }

        // --- Set "example" data for the server ---
        val exampleTemperature = 25.0 // 25°C
        val exampleHumidity = 60.0   // 60%
        val exampleStorage = "Refrigerated" // MUST be a value your server's encoder knows!
        // ---

        // Call Model 2 (The Server)
        viewModelScope.launch {
            val request = ShelfLifeRequest(
                dish_name = result.label,
                temperature = exampleTemperature,
                humidity = exampleHumidity,
                storage = exampleStorage
            )

            try {
                val response = apiService.predictShelfLife(request)

                if (response.isSuccessful && response.body() != null) {
                    // SUCCESS!
                    _uiState.update {
                        it.copy(
                            isLoadingNetwork = false,
                            predictionResult = response.body()!!.formatted
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: response.message()
                    _uiState.update {
                        it.copy(isLoadingNetwork = false, errorMessage = "API Error: $errorBody")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoadingNetwork = false, errorMessage = "Network Error: ${e.message}")
                }
            }
        }
    }

    /**
     * Called when user closes the bottom panel or selects a new image.
     */
    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedFood = null,
                predictionResult = null,
                errorMessage = null,
                isLoadingNetwork = false
            )
        }
    }

    // --- Private Logic (Your original functions, slightly modified) ---

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