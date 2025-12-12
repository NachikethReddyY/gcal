package com.ynr.gcal.ui.capture

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ynr.gcal.R
import com.ynr.gcal.data.local.MealLog
import com.ynr.gcal.ui.theme.DeepBlue
import com.ynr.gcal.ui.theme.EnergeticOrange
import java.nio.ByteBuffer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureSheet(
    viewModel: CaptureViewModel,
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onAnalysisSuccess: () -> Unit
) {
    var mode by remember { mutableStateOf<CaptureMode>(CaptureMode.Selection) }
    val context = LocalContext.current
    
    // Camera Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onCameraClick()
        } else {
            // Permission denied logic
        }
    }

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
            viewModel.capturedBitmap = bitmap
        }
    }

    LaunchedEffect(viewModel.analysisResult) {
        if (viewModel.analysisResult != null) {
            onAnalysisSuccess()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                viewModel.isAnalyzing -> {
                    CircularProgressIndicator(color = DeepBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI is analyzing food...", color = DeepBlue)
                }
                viewModel.capturedBitmap != null -> {
                    ImageConfirmView(viewModel)
                }
                mode == CaptureMode.Selection -> {
                    Text("Log Meal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
//...

                        CaptureOption(
                            icon = Icons.Filled.Add,
                            label = "Camera",
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    onCameraClick()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        )
                        // Gallery button removed as per request (moved to Camera Screen)
                        
                        CaptureOption(
                            icon = Icons.Default.Create,
                            label = "Text",
                            onClick = { mode = CaptureMode.Text }
                        )
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
                mode == CaptureMode.Text -> {
                    TextEntryView(viewModel)
                }
            }
            
            if (viewModel.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(viewModel.error!!, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
fun TextEntryView(viewModel: CaptureViewModel) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("What did you eat?") },
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = { viewModel.analyzeText(text) },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Text("Analyze")
    }
}

@Composable
fun ImageConfirmView(viewModel: CaptureViewModel) {
    var hint by remember { mutableStateOf("") }
    Column {
        viewModel.capturedBitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.height(200.dp))
        }
        OutlinedTextField(
            value = hint,
            onValueChange = { hint = it },
            label = { Text("Add a hint (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { viewModel.analyzeImage(viewModel.capturedBitmap!!, hint) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Analyze")
        }
    }
}

// ResultView removed (moved to MealResultScreen)

@Composable
fun CaptureOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(48.dp),
            tint = DeepBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

enum class CaptureMode { Selection, Camera, Text }
