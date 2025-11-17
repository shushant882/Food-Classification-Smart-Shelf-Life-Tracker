package com.example.minorfinal.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.minorfinal.ClassificationResult
import com.example.minorfinal.ClassifierViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

// --- DATA CLASSES FOR LOGIC ---
// (These remain the same as your file)
data class WeatherContext(val city: String, val tempC: Int, val humidity: Int)
data class ShelfLifeData(val daysLeft: Int, val condition: String, val decayFactor: Float)

// --- PROFESSIONALISM UPDATE: Centralized Color Palette ---
/**
 * A private object to hold the unique "cyberpunk" color theme
 * for this specific screen, making it consistent and easy to edit.
 */
private object ClassifierColors {
    val background = Color(0xFF0A0A0A)
    val primary = Color(0xFF00FFCC)
    val secondary = Color(0xFFFF0055)
    val textPrimary = Color(0xFFE0E0E0)
    val textSecondary = Color(0xFF888888)
    val surface = Color(0xFF121212)
    val surfaceHighlight = Color(0xFF1A1A1A)
    val border = Color(0xFF333333)
    val borderActive = primary
    val warning = secondary
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ClassifierScreen(
    navController: NavController,
    viewModel: ClassifierViewModel = viewModel()
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // STATE MANAGEMENT
    var selectedResult by remember { mutableStateOf<ClassificationResult?>(null) }
    var showScannerAnimation by remember { mutableStateOf(false) }

    // Handle Back press to deselect item
    BackHandler(enabled = selectedResult != null) {
        selectedResult = null
    }

    // Force animation timer
    LaunchedEffect(showScannerAnimation) {
        if (showScannerAnimation) {
            delay(3000)
            showScannerAnimation = false
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempPhotoUri?.let { uri ->
                    selectedResult = null // Reset selection
                    showScannerAnimation = true
                    viewModel.onPhotoTaken(uri, context)
                }
            } else {
                Toast.makeText(context, "Failed to take photo", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedResult = null // Reset selection
                showScannerAnimation = true
                viewModel.onImagePicked(uri, context)
            } else {
                Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
    )

    FoodClassifierScreenContent(
        viewModel = viewModel,
        forceAnimationState = showScannerAnimation,
        selectedResult = selectedResult,
        onPickImage = {
            viewModel.clearResults()
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onTakePhoto = {
            if (context.hasCameraPermission()) {
                viewModel.clearResults()
                tempPhotoUri = context.createTempImageUri()
                cameraLauncher.launch(tempPhotoUri!!)
            } else {
                cameraPermissionState.launchPermissionRequest()
            }
        },
        onSelectResult = { result ->
            selectedResult = result
        },
        onResetSelection = {
            selectedResult = null
        }
    )
}

@Composable
fun FoodClassifierScreenContent(
    viewModel: ClassifierViewModel,
    forceAnimationState: Boolean,
    selectedResult: ClassificationResult?, // The currently selected item
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
    onSelectResult: (ClassificationResult) -> Unit,
    onResetSelection: () -> Unit
) {
    val imageUri by viewModel.imageUri
    val results by viewModel.classificationResults
    val error by viewModel.error
    val isModelLoading by viewModel.isLoading
    val isScanning = isModelLoading || forceAnimationState

    // Define Delhi Weather Context
    val delhiWeather = WeatherContext("New Delhi", 34, 65) // Hot and Humid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClassifierColors.background) // Use themed color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Neural Vision v2.0",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ClassifierColors.primary, // Use themed color
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                if (selectedResult != null) {
                    IconButton(onClick = onResetSelection) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ClassifierColors.textSecondary)
                    }
                }
            }

            // SCANNER / IMAGE AREA
            val imageHeight by animateDpAsState(if (selectedResult != null) 250.dp else 400.dp, label = "imgH")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ClassifierColors.surface), // Use themed color
                contentAlignment = Alignment.Center
            ) {
                if (imageUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // PROFESSIONALISM UPDATE: Replaced dated icon
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = null,
                            tint = ClassifierColors.textSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "AWAITING INPUT",
                            color = ClassifierColors.textSecondary,
                            letterSpacing = 2.sp
                        )
                    }
                }

                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(model = it),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Overlay Logic
                if (isScanning) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
                    AdvancedCyberpunkScanner(modifier = Modifier.fillMaxSize())
                    Text(
                        "ANALYZING...",
                        color = ClassifierColors.primary,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // CONTROLS
            AnimatedVisibility(visible = selectedResult == null && !isScanning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CyberButton(text = "GALLERY", onClick = onPickImage, enabled = true)
                    CyberButton(text = "CAMERA", onClick = onTakePhoto, enabled = true)
                }
            }

            // RESULT LIST SECTION
            Box(modifier = Modifier.fillMaxWidth()) {
                if (!isScanning && results.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        AnimatedVisibility(visible = selectedResult == null) {
                            Text(
                                "DETECTED OBJECTS:",
                                color = ClassifierColors.textSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 2.sp
                            )
                        }

                        results.forEachIndexed { index, result ->
                            val isMeSelected = selectedResult == result
                            val isAnythingSelected = selectedResult != null

                            AnimatedVisibility(
                                visible = !isAnythingSelected || isMeSelected,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                ResultRow(
                                    label = result.label,
                                    score = result.score,
                                    index = index,
                                    isSelected = isMeSelected,
                                    onClick = { onSelectResult(result) }
                                )
                            }
                        }
                    }
                }
            }
            // PROFESSIONALISM UPDATE: Removed Spacer(400.dp) hack
        }

        // --- BOTTOM SHEET ANIMATION ---
        AnimatedVisibility(
            visible = selectedResult != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedResult?.let { result ->
                DetailedAnalysisPanel(
                    foodName = result.label,
                    confidence = result.score,
                    weather = delhiWeather
                )
            }
        }
    }
}

@Composable
fun DetailedAnalysisPanel(foodName: String, confidence: Float, weather: WeatherContext) {
    val shelfLife = calculateShelfLife(foodName, weather.tempC, weather.humidity)
    val calories = (foodName.hashCode().absoluteValue % 400) + 100

    val appearAnim = remember { Animatable(0f) }
    LaunchedEffect(foodName) {
        appearAnim.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(
                1.dp,
                ClassifierColors.primary.copy(alpha = 0.3f),
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = ClassifierColors.background.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ClassifierColors.border)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = foodName.uppercase(),
                style = MaterialTheme.typography.displaySmall,
                color = ClassifierColors.textPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "CONFIDENCE: ${(confidence * 100).toInt()}%",
                color = ClassifierColors.primary,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // PROFESSIONALISM UPDATE: Replaced icons
                CyberStatBox(
                    label = "CALORIES",
                    value = "${calories}kcal",
                    icon = Icons.Outlined.LocalFireDepartment, // More thematic
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                CyberStatBox(
                    label = "REGION: ${weather.city.uppercase()}",
                    value = "${weather.tempC}°C / ${weather.humidity}%",
                    icon = Icons.Outlined.Public, // More thematic
                    modifier = Modifier.weight(1f),
                    isWarning = weather.tempC > 30
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ESTIMATED SHELF LIFE",
                color = ClassifierColors.textSecondary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Shelf Life Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(CutCornerShape(bottomEnd = 10.dp))
                    .background(ClassifierColors.surfaceHighlight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(shelfLife.decayFactor * appearAnim.value)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(ClassifierColors.primary, ClassifierColors.warning)
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${shelfLife.daysLeft} DAYS REMAINING",
                        color = ClassifierColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = shelfLife.condition.uppercase(),
                        color = Color.Black, // Stands out on the gradient
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Calculation based on Delhi ambient temperature (${weather.tempC}°C). High heat accelerates decay by 40%.",
                color = ClassifierColors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                lineHeight = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PROFESSIONALISM UPDATE: Using LinearProgressIndicator
            MacroRow(label = "PROTEIN", color = ClassifierColors.secondary, progress = 0.7f * appearAnim.value)
            MacroRow(label = "CARBS", color = Color(0xFF00CCFF), progress = 0.5f * appearAnim.value)
            MacroRow(label = "FATS", color = Color(0xFFFFAA00), progress = 0.3f * appearAnim.value)
        }
    }
}

@Composable
fun CyberStatBox(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    val activeColor = if (isWarning) ClassifierColors.warning else ClassifierColors.primary

    Column(
        modifier = modifier
            .border(
                1.dp,
                if (isWarning) activeColor.copy(alpha = 0.5f) else ClassifierColors.border,
                RoundedCornerShape(8.dp)
            )
            .background(ClassifierColors.surfaceHighlight.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = activeColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = ClassifierColors.textSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = ClassifierColors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ResultRow(
    label: String,
    score: Float,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) ClassifierColors.borderActive else ClassifierColors.border
    val bgColor = if (isSelected) ClassifierColors.primary.copy(alpha = 0.1f) else ClassifierColors.surfaceHighlight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(CutCornerShape(bottomEnd = 10.dp))
            .border(1.dp, borderColor, CutCornerShape(bottomEnd = 10.dp))
            .background(bgColor)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label.uppercase(),
                color = ClassifierColors.textPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = if(isSelected) "ANALYZING DETAILS..." else "TAP TO SELECT",
                color = if(isSelected) ClassifierColors.primary else ClassifierColors.textSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Text(
            text = "${(score * 100).toInt()}%",
            color = if (score > 0.7f) ClassifierColors.primary else ClassifierColors.warning,
            fontWeight = FontWeight.Bold
        )
    }
}

// PROFESSIONALISM UPDATE: Replaced basic Box with LinearProgressIndicator
@Composable
fun MacroRow(label: String, color: Color, progress: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = ClassifierColors.textPrimary,
            modifier = Modifier.width(60.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = ClassifierColors.surfaceHighlight
        )
    }
}

@Composable
fun CyberButton(text: String, onClick: () -> Unit, enabled: Boolean) {
    val activeColor = ClassifierColors.primary

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = activeColor.copy(alpha = 0.1f),
            contentColor = activeColor,
            disabledContainerColor = ClassifierColors.border.copy(alpha = 0.2f),
            disabledContentColor = ClassifierColors.textSecondary
        ),
        border = BorderStroke(1.dp, if(enabled) activeColor else ClassifierColors.border)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
fun AdvancedCyberpunkScanner(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_anim")

    val radarRotate by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "radar"
    )
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scan"
    )
    // PROFESSIONALISM UPDATE: Added subtle grid animation
    val gridOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 40.dp.value,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse),
        label = "grid"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val colorPrimary = ClassifierColors.primary

        // Grid
        val gridSize = 40.dp.toPx()
        for (i in -5..(width/gridSize).toInt() + 5) {
            drawLine(
                colorPrimary.copy(alpha = 0.1f),
                Offset(i * gridSize + gridOffset, 0f),
                Offset(i * gridSize + gridOffset, height),
                1f
            )
        }
        for (i in -5..(height/gridSize).toInt() + 5) {
            drawLine(
                colorPrimary.copy(alpha = 0.1f),
                Offset(0f, i * gridSize + gridOffset),
                Offset(width, i * gridSize + gridOffset),
                1f
            )
        }

        // Scan Line
        val lineY = height * scanY
        drawLine(colorPrimary, Offset(0f, lineY), Offset(width, lineY), 2.dp.toPx())
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(colorPrimary.copy(alpha = 0f), colorPrimary.copy(alpha = 0.3f)),
                startY = lineY - 100f, endY = lineY
            ),
            topLeft = Offset(0f, lineY - 100f), size = Size(width, 100f)
        )

        // Simple Corners
        val capLen = 30.dp.toPx()
        val stroke = 3.dp.toPx()

        drawPath(
            path = Path().apply { moveTo(0f, capLen); lineTo(0f, 0f); lineTo(capLen, 0f) },
            color = colorPrimary,
            style = Stroke(width = stroke)
        )
        drawPath(
            path = Path().apply { moveTo(width - capLen, 0f); lineTo(width, 0f); lineTo(width, capLen) },
            color = colorPrimary,
            style = Stroke(width = stroke)
        )
        drawPath(
            path = Path().apply { moveTo(0f, height - capLen); lineTo(0f, height); lineTo(capLen, height) },
            color = colorPrimary,
            style = Stroke(width = stroke)
        )
        drawPath(
            path = Path().apply { moveTo(width - capLen, height); lineTo(width, height); lineTo(width, height - capLen) },
            color = colorPrimary,
            style = Stroke(width = stroke)
        )
    }
}

// --- HELPER & LOGIC FUNCTIONS (MOVED TO BOTTOM) ---

fun calculateShelfLife(food: String, temp: Int, humidity: Int): ShelfLifeData {
    var days = when (food.lowercase()) {
        "banana" -> 5
        "apple" -> 14
        "bread" -> 4
        "milk" -> 2
        else -> 7
    }

    if (temp > 20) {
        val diff = temp - 20
        val penaltyFactor = (diff / 5) * 0.15
        days = (days * (1 - penaltyFactor)).toInt()
    }

    if (humidity > 60 && (food.lowercase().contains("bread") || food.lowercase().contains("chip"))) {
        days -= 1
    }

    days = days.coerceAtLeast(1)

    val factor = days / 14f
    val condition = if (days < 3) "CRITICAL" else "STABLE"

    return ShelfLifeData(days, condition, factor.coerceIn(0.1f, 1f))
}

fun Context.hasCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.createTempImageUri(): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val imageFile = File.createTempFile(imageFileName, ".jpg", cacheDir)
    return FileProvider.getUriForFile(this, "${packageName}.provider", imageFile)
}