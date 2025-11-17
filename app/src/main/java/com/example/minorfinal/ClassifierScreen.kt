package com.example.minorfinal.ui.screens // Your package name

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location // <-- IMPORT THIS
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
import androidx.compose.foundation.horizontalScroll
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
import com.example.minorfinal.ClassifierUiState
import com.example.minorfinal.ClassifierViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

// --- DATA CLASSES (Your original) ---
data class WeatherContext(val city: String, val tempC: Int, val humidity: Int)

// --- Color Palette (Your original) ---
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

    val uiState by viewModel.uiState.collectAsState()
    var showScannerAnimation by remember { mutableStateOf(false) }

    BackHandler(enabled = uiState.selectedFood != null) {
        viewModel.clearSelection()
    }

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
                    showScannerAnimation = true
                    viewModel.onImageSelected(uri)
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
                showScannerAnimation = true
                viewModel.onImageSelected(uri)
            } else {
                Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
    )

    FoodClassifierScreenContent(
        uiState = uiState,
        forceAnimationState = showScannerAnimation,
        onPickImage = {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onTakePhoto = {
            if (context.hasCameraPermission()) {
                tempPhotoUri = context.createTempImageUri()
                cameraLauncher.launch(tempPhotoUri!!)
            } else {
                cameraPermissionState.launchPermissionRequest()
            }
        },
        onSelectResult = { result ->
            viewModel.onFoodSelected(result)
        },
        onResetSelection = {
            viewModel.clearSelection()
        },
        // --- PASS NEW FUNCTIONS ---
        onStorageChange = { viewModel.onStorageChanged(it) },
        onGetPrediction = { lat, lon -> viewModel.getPrediction(lat, lon) } // Pass the VM function
    )
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FoodClassifierScreenContent(
    uiState: ClassifierUiState,
    forceAnimationState: Boolean,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
    onSelectResult: (ClassificationResult) -> Unit,
    onResetSelection: () -> Unit,
    // --- RECEIVE NEW FUNCTIONS ---
    onStorageChange: (String) -> Unit,
    onGetPrediction: (Double, Double) -> Unit // The function from the ViewModel
) {
    val imageUri = uiState.imageUri
    val results = uiState.classificationResults
    val selectedResult = uiState.selectedFood
    val isModelLoading = uiState.isLoadingModel
    val isScanning = isModelLoading || forceAnimationState

    val context = LocalContext.current

    // --- 1. SET UP LOCATION PERMISSION AND LAUNCHER ---
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // This function will get the location and call the ViewModel
    val requestLocationAndPredict = {
        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request the *current* location, not the last known one
            locationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        onGetPrediction(location.latitude, location.longitude)
                    } else {
                        Toast.makeText(context, "Could not get location. Is GPS on?", Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, "Error getting location: ${it.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // This should not happen if logic is correct, but as a fallback
            locationPermissionState.launchPermissionRequest()
        }
    }

    // This launcher is called after the permission dialog
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission was just granted, now get location
                requestLocationAndPredict()
            } else {
                // Permission is DENIED
                Toast.makeText(context, "Location permission is required for this feature.", Toast.LENGTH_LONG).show()
            }
        }
    )

    // --- 🎨 NEW GRADIENT BRUSH 🎨 ---
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            ClassifierColors.surface, // Lighter color at the top
            ClassifierColors.background // Darker color at the bottom
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush) // <-- 🎨 GRADIENT APPLIED HERE 🎨
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
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FreshLens v2.0",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ClassifierColors.primary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

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
                    .background(ClassifierColors.surface),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
        }

        // --- BOTTOM SHEET ANIMATION ---
        AnimatedVisibility(
            visible = uiState.selectedFood != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            DetailedAnalysisPanel(
                uiState = uiState,
                onStorageChange = onStorageChange,
                onPredictClick = {
                    // --- THIS IS THE NEW LOGIC ---
                    if (locationPermissionState.status.isGranted) {
                        // Permission is already granted
                        requestLocationAndPredict()
                    } else {
                        // Permission is not granted, launch the request
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                }
            )
        }
    }
}

// --- DETAILED ANALYSIS PANEL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedAnalysisPanel(
    uiState: ClassifierUiState,
    onStorageChange: (String) -> Unit,
    onPredictClick: () -> Unit
) {
    // Get the data from the uiState
    val foodName = uiState.selectedFood?.label ?: "Error"
    val confidence = uiState.selectedFood?.score ?: 0f
    val shelfLifeResult = uiState.predictionResult
    val isLoading = uiState.isLoadingNetwork
    val errorMessage = uiState.errorMessage

    // Your local logic
    val calories = (foodName.hashCode().absoluteValue % 400) + 100

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f), // Increased height slightly
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = BorderStroke(1.dp, ClassifierColors.primary.copy(alpha = 0.3f)),
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

            // --- THIS IS THE NEW INPUT FORM ---
            // It will only show if there is NO result yet
            AnimatedVisibility(visible = shelfLifeResult == null) {
                Column {
                    Text(
                        text = "ENVIRONMENTAL FACTORS",
                        color = ClassifierColors.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // --- Storage Options ---
                    Text(
                        text = "STORAGE CONDITION",
                        color = ClassifierColors.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    // A horizontal, scrollable row for the options
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()), // Makes it scroll if needed
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Loop over the options from the ViewModel
                        uiState.storageOptions.forEach { option ->
                            val isSelected = (uiState.storageInput == option)
                            CyberStorageChip(
                                text = option,
                                isSelected = isSelected,
                                onClick = { onStorageChange(option) } // Calls the ViewModel
                            )
                        }
                    }
                    // --- End of Storage ---


                    Spacer(Modifier.height(20.dp))

                    // Predict Button
                    Button(
                        onClick = onPredictClick,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ClassifierColors.primary,
                            contentColor = ClassifierColors.background,
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = ClassifierColors.background,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("GET SHELF LIFE (USES LOCATION)", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }
            }

            // --- THIS IS THE RESULT SECTION ---
            // It will only show if there IS a result
            AnimatedVisibility(visible = shelfLifeResult != null) {
                Column {
                    Text(
                        text = "ESTIMATED SHELF LIFE",
                        color = ClassifierColors.textPrimary, // <-- Your change
                        style = MaterialTheme.typography.labelMedium, // <-- Your change
                        modifier = Modifier.align(Alignment.CenterHorizontally) // <-- Your change
                    )
                    Spacer(Modifier.height(8.dp))

                    // --- 🎨 GRADIENT BACKGROUND ADDED HERE 🎨 ---
                    val infiniteTransition = rememberInfiniteTransition()

                    val xOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 2000f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(8000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart   // 👈 Smooth continuous movement
                        )
                    )

                    val yOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1500f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(5000, easing = EaseInOut),
                            repeatMode = RepeatMode.Restart   // 👈 No reverse flicker
                        )
                    )

                    val backgroundGradientBrush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00FF6F),
                            Color(0xFF1AFFB5), //
                            Color(0xFF00FFC3), //
                             Color(0xFF00E7FF), // cyan glow
                             Color(0xFF00C4FF)
                        ),
                        start = Offset(xOffset, yOffset),
                        end = Offset(xOffset - 800f, yOffset - 800f)
                    )


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(brush = backgroundGradientBrush) // <-- CHANGED
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // --- 🎨 GRADIENT TEXT 🎨 ---
                        val textGradientBrush = Brush.horizontalGradient(
                            colors = listOf(
                                ClassifierColors.primary,
                                ClassifierColors.secondary
                            )
                        )

                        Text(
                            text = shelfLifeResult?.uppercase() ?: "",
                            fontWeight = FontWeight.Bold,
                            // Apply the brush to the style
                            style = MaterialTheme.typography.labelLarge.copy(
                                //brush = textGradientBrush
                            )
                        )
                        // --- END OF GRADIENT TEXT ---
                    }
                }
            }

            // Show error message if it exists
            if (errorMessage != null && shelfLifeResult == null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = ClassifierColors.warning,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Your original stats (Macros, etc.)
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CyberStatBox(
                    label = "CALORIES",
                    value = "${calories} kcal/100gm", // <-- Your change
                    icon = Icons.Outlined.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))

                // --- THIS IS THE UPDATED WEATHER BOX ---
                // Get weather from the uiState
                val city = uiState.fetchedCity
                val temp = uiState.fetchedTemp
                val humidity = uiState.fetchedHumidity

                // Determine text for the stat box
                val regionLabel = if (city != null) "REGION: ${city.uppercase()}" else "LOCATION"
                val regionValue = if (temp != null && humidity != null) {
                    "${"%.1f".format(temp)}°C / $humidity%"
                } else if (uiState.isLoadingNetwork) {
                    "Fetching..."
                } else {
                    "Pending..." // Before button is pressed
                }

                CyberStatBox(
                    label = regionLabel,
                    value = regionValue,
                    icon = Icons.Outlined.Public,
                    modifier = Modifier.weight(1f),
                    isWarning = temp?.let { it > 30 } ?: false
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // --- UPDATED INFO TEXT ---
            val temp = uiState.fetchedTemp
            val infoText = if (temp != null) {
                "Calculation based on real-time ambient temperature (${"%.1f".format(temp)}°C)."
            } else {
                "Calculation will be based on real-time ambient temperature."
            }
            Text(
                text = infoText,
                color = ClassifierColors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp, // <-- Your change
                lineHeight = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- NEW MACRO SECTION ---
            val appearAnim = remember { Animatable(0f) }
            LaunchedEffect(foodName) {
                appearAnim.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
            }

            // New Row for the circular indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                MacroCircularIndicator(
                    label = "PROTEIN",
                    color = ClassifierColors.secondary,
                    progress = 0.7f * appearAnim.value, // Example value
                    modifier = Modifier.weight(1f)
                )
                MacroCircularIndicator(
                    label = "CARBS",
                    color = Color(0xFF00CCFF),
                    progress = 0.5f * appearAnim.value, // Example value
                    modifier = Modifier.weight(1f)
                )
                MacroCircularIndicator(
                    label = "FATS",
                    color = Color(0xFFFFAA00),
                    progress = 0.3f * appearAnim.value, // Example value
                    modifier = Modifier.weight(1f)
                )
            }
            // --- END OF NEW MACRO SECTION ---
        }
    }
}

// --- ADD THIS NEW COMPOSABLE ---
@Composable
fun CyberStorageChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = ClassifierColors.primary
    val inactiveColor = ClassifierColors.border

    // Determine colors based on selection
    val bgColor = if (isSelected) activeColor.copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (isSelected) activeColor else ClassifierColors.textSecondary
    val borderColor = if (isSelected) activeColor else inactiveColor

    Box(
        modifier = Modifier
            .clip(CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp))
            .border(1.dp, borderColor, CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp) // Made it taller
    ) {
        Text(
            text = text.uppercase(), // Make it match your style
            color = textColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

// --- ALL YOUR ORIGINAL COMPOSABLES ---

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
                text = if(isSelected) "ENTER DETAILS..." else "TAP TO SELECT", // Changed text
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

// --- THIS IS THE NEW MACRO COMPOSABLE ---
@Composable
fun MacroCircularIndicator(
    label: String,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Background track
            CircularProgressIndicator(
                progress = { 1f }, // Full circle
                modifier = Modifier.size(70.dp),
                color = ClassifierColors.surfaceHighlight,
                strokeWidth = 6.dp,
            )
            // Foreground progress
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(70.dp),
                color = color,
                strokeWidth = 6.dp,
                strokeCap = StrokeCap.Round // Makes the line end rounded
            )
            // Text inside
            Text(
                text = "${(progress * 100).toInt()}", // Removed % to fit
                color = ClassifierColors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            color = ClassifierColors.textSecondary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun CyberButton(text: String, onClick: () -> Unit, enabled: Boolean) {
    val activeColor = ClassifierColors.primary

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CutCornerShape( 10.dp),
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

    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scan"
    )
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

// --- (Keep your original helper functions) ---
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