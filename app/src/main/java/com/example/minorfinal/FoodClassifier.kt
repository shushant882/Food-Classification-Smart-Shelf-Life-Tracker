package com.example.minorfinal

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * The main classifier class.
 * This class handles loading the TFLite model and running inference.
 */
class FoodClassifier(private val context: Context) {

    // --- 1. CONFIGURATION ---
    companion object {
        private const val MODEL_FILE_NAME = "dish_classifier.tflite" // Your model file in assets
        private const val LABEL_FILE_NAME = "Dishes_names_359mb.txt"   // Your labels file in assets
        private const val INPUT_IMAGE_WIDTH = 224
        private const val INPUT_IMAGE_HEIGHT = 224
        private const val NORMALIZE_MEAN = 0f
        private const val NORMALIZE_STD = 255f
    }
    // --- -------------------- ---

    private var interpreter: Interpreter
    private var labels: List<String>
    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0

    init {
        try {
            val modelBuffer = loadModelFile(context, MODEL_FILE_NAME)
            interpreter = Interpreter(modelBuffer)
            labels = FileUtil.loadLabels(context, LABEL_FILE_NAME)

            val inputTensor = interpreter.getInputTensor(0)
            val inputShape = inputTensor.shape()
            inputImageWidth = inputShape[1]
            inputImageHeight = inputShape[2]

            Log.i("FoodClassifier", "Model loaded successfully. Input shape: $inputImageWidth x $inputImageHeight")

        } catch (e: Exception) {
            Log.e("FoodClassifier", "Error initializing classifier", e)
            throw IllegalStateException("Failed to initialize classifier", e)
        }
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun preprocessImage(bitmap: Bitmap): TensorImage {
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(NORMALIZE_MEAN, NORMALIZE_STD))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)
        return tensorImage
    }

    fun classify(bitmap: Bitmap): List<ClassificationResult> {
        try {
            val tensorImage = preprocessImage(bitmap)
            val outputTensor = interpreter.getOutputTensor(0)
            val outputShape = outputTensor.shape()
            val outputDataType = outputTensor.dataType()
            val outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType)

            interpreter.run(tensorImage.buffer, outputBuffer.buffer.rewind())

            val probabilityMap = TensorLabel(labels, outputBuffer).mapWithFloatValue

            return probabilityMap.entries.map { (label, score) ->
                ClassificationResult(label, score)
            }
                .sortedByDescending { it.score }
                .take(3)

        } catch (e: Exception) {
            Log.e("FoodClassifier", "Error running classification", e)
            return emptyList()
        }
    }
}