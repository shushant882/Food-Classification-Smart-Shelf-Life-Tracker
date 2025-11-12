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
 * The ViewModel for our app. It holds the UI state and manages interactions
 * between the UI (MainActivity) and the classifier logic (FoodClassifier).
 */
class MainViewModel : ViewModel() {

    // --- UI State ---

    // The URI of the image selected by the user (from gallery or camera)
    val imageUri = mutableStateOf<Uri?>(null)

    // The list of results from the classifier (e.g., "Pizza, 90.5%")
    val classificationResults = mutableStateOf<List<ClassificationResult>>(emptyList())

    // A simple error message to show to the user
    val error = mutableStateOf<String?>(null)

    // True when the classifier is busy running
    val isLoading = mutableStateOf(false)

    // --- Classifier ---

    // Lazily initialize the classifier. It will only be created when first needed.
    private lateinit var classifier: FoodClassifier

    // Initializes the FoodClassifier. Must be called with a Context.
    private fun initClassifier(context: Context) {
        // Ensure it's only initialized once
        if (!::classifier.isInitialized) {
            try {
                classifier = FoodClassifier(context)
            } catch (e: Exception) {
                error.value = "Failed to initialize classifier: ${e.message}"
            }
        }
    }

    // --- Public Functions (Called by the UI) ---

    /**
     * Called when the user picks an image from the gallery.
     */
    fun onImagePicked(uri: Uri, context: Context) {
        imageUri.value = uri
        runClassification(context)
    }

    /**
     * Called when the user takes a photo with the camera.
     */
    fun onPhotoTaken(uri: Uri, context: Context) {
        imageUri.value = uri
        runClassification(context)
    }

    /**
     * Called when the user clicks the "Clear" button.
     */
    fun clearResults() {
        imageUri.value = null
        classificationResults.value = emptyList()
        error.value = null
    }

    // --- Private Logic ---

    /**
     * The main function that loads the bitmap and runs the classifier.
     */
    private fun runClassification(context: Context) {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            // Ensure classifier is ready before using it
            initClassifier(context)
            if (!::classifier.isInitialized) {
                isLoading.value = false
                return@launch // Classifier failed to init, error is already set
            }

            // Load the Bitmap from the URI
            val bitmap = loadBitmapFromUri(uri = imageUri.value!!, context = context)
            if (bitmap == null) {
                error.value = "Failed to load image."
                isLoading.value = false
                return@launch
            }

            // Run classification on a background thread (Dispatchers.Default)
            val results = withContext(Dispatchers.Default) {
                classifier.classify(bitmap)
            }

            // Update the UI state with the results
            classificationResults.value = results
            isLoading.value = false
        }
    }

    /**
     * Helper function to load a Bitmap from a content URI.
     */
    private fun loadBitmapFromUri(uri: Uri, context: Context): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Use ImageDecoder for Android 9 (Pie) and newer
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                // Use deprecated MediaStore for older Android versions
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            error.value = "Error loading bitmap: ${e.message}"
            Log.e("MainViewModel", "Error loading bitmap", e)
            null
        }
    }
}