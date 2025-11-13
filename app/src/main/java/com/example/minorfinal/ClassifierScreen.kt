package com.example.minorfinal

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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

// --- DATA CLASSES FOR LOGIC ---
data class WeatherContext(val city: String, val tempC: Int, val humidity: Int)
data class ShelfLifeData(val daysLeft: Int, val condition: String, val decayFactor: Float)

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
            .background(Color(0xFF050505))
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
                    color = Color(0xFF00FFCC),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                if (selectedResult != null) {
                    IconButton(onClick = onResetSelection) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
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
                    .background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = rememberAsyncImagePainter(model = android.R.drawable.ic_menu_camera),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text("AWAITING INPUT", color = Color.Gray, letterSpacing = 2.sp)
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
                        color = Color(0xFF00FFCC),
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
                                color = Color.Gray,
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
            Spacer(modifier = Modifier.height(400.dp))
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
        appearAnim.animateTo(1f, tween(1000))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.3f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E0E0E))
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
                    .background(Color.DarkGray)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = foodName.uppercase(),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "CONFIDENCE: ${(confidence * 100).toInt()}%",
                color = Color(0xFF00FFCC),
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CyberStatBox(
                    label = "CALORIES",
                    value = "${calories}kcal",
                    icon = Icons.Default.Home,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                CyberStatBox(
                    label = "REGION: ${weather.city.uppercase()}",
                    value = "${weather.tempC}°C / ${weather.humidity}%",
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f),
                    isWarning = weather.tempC > 30
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ESTIMATED SHELF LIFE",
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(CutCornerShape(bottomEnd = 10.dp))
                    .background(Color(0xFF1A1A1A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(shelfLife.decayFactor * appearAnim.value)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF00FFCC), Color(0xFFFF0055))
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
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = shelfLife.condition.uppercase(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Calculation based on Delhi ambient temperature (${weather.tempC}°C). High heat accelerates decay by 40%.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                lineHeight = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            MacroRow(label = "PROTEIN", color = Color(0xFFFF0055), progress = 0.7f * appearAnim.value)
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
    Column(
        modifier = modifier
            .border(1.dp, if(isWarning) Color(0xFFFF0055) else Color(0xFF333333), RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A1A).copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if(isWarning) Color(0xFFFF0055) else Color(0xFF00FFCC),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = Color.White,
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
    val borderColor = if (isSelected) Color(0xFF00FFCC) else Color(0xFF333333)
    val bgColor = if (isSelected) Color(0xFF00FFCC).copy(alpha = 0.1f) else Color(0xFF1A1A1A)

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
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = if(isSelected) "ANALYZING DETAILS..." else "TAP TO SELECT",
                color = if(isSelected) Color(0xFF00FFCC) else Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Text(
            text = "${(score * 100).toInt()}%",
            color = if (score > 0.7f) Color(0xFF00FFCC) else Color(0xFFFF0055),
            fontWeight = FontWeight.Bold
        )
    }
}

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
            color = Color.White,
            modifier = Modifier.width(60.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.DarkGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(color)
            )
        }
    }
}

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

@Composable
fun CyberButton(text: String, onClick: () -> Unit, enabled: Boolean) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF00FFCC).copy(alpha = 0.1f),
            contentColor = Color(0xFF00FFCC),
            disabledContainerColor = Color.DarkGray.copy(alpha = 0.2f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if(enabled) Color(0xFF00FFCC) else Color.Gray)
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

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val colorPrimary = Color(0xFF00FFCC)

        // Grid
        val gridSize = 40.dp.toPx()
        for (i in 0..(width/gridSize).toInt()) {
            drawLine(colorPrimary.copy(alpha = 0.1f), Offset(i * gridSize, 0f), Offset(i * gridSize, height), 1f)
        }
        for (i in 0..(height/gridSize).toInt()) {
            drawLine(colorPrimary.copy(alpha = 0.1f), Offset(0f, i * gridSize), Offset(width, i * gridSize), 1f)
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

        // Simple Corners - CORRECTED WITH NAMED ARGUMENTS
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