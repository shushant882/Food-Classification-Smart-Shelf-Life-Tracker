package com.example.minorfinal



import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The ViewModel for the Classifier screen. It holds the UI state and manages
 * interactions between the UI and the FoodClassifier.
 */
class ClassifierViewModel : ViewModel() {

    // --- UI State ---
    val imageUri = mutableStateOf<Uri?>(null)
    val classificationResults = mutableStateOf<List<ClassificationResult>>(emptyList())
    val error = mutableStateOf<String?>(null)
    val isLoading = mutableStateOf(false)

    // --- Classifier ---
    private lateinit var classifier: FoodClassifier

    private fun initClassifier(context: Context) {
        if (!::classifier.isInitialized) {
            try {
                classifier = FoodClassifier(context)
            } catch (e: Exception) {
                error.value = "Failed to initialize classifier: ${e.message}"
            }
        }
    }

    // --- Public Functions (Called by the UI) ---

    fun onImagePicked(uri: Uri, context: Context) {
        imageUri.value = uri
        runClassification(context)
    }

    fun onPhotoTaken(uri: Uri, context: Context) {
        imageUri.value = uri
        runClassification(context)
    }

    fun clearResults() {
        imageUri.value = null
        classificationResults.value = emptyList()
        error.value = null
    }

    // --- Private Logic ---

    private fun runClassification(context: Context) {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            initClassifier(context)
            if (!::classifier.isInitialized) {
                isLoading.value = false
                return@launch
            }

            val bitmap = loadBitmapFromUri(uri = imageUri.value!!, context = context)
            if (bitmap == null) {
                error.value = "Failed to load image."
                isLoading.value = false
                return@launch
            }

            val results = withContext(Dispatchers.Default) {
                classifier.classify(bitmap)
            }

            classificationResults.value = results
            isLoading.value = false
        }
    }

    private fun loadBitmapFromUri(uri: Uri, context: Context): Bitmap? {
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
            error.value = "Error loading bitmap: ${e.message}"
            Log.e("ClassifierViewModel", "Error loading bitmap", e)
            null
        }
    }
}