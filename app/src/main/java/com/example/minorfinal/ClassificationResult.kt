package com.example.minorfinal


/**
 * A data class to hold a single classification result.
 * @param label The friendly name of the food (e.g., "Pizza").
 * @param score The confidence score (0.0 to 1.0).
 */
data class ClassificationResult(
    val label: String,
    val score: Float
)