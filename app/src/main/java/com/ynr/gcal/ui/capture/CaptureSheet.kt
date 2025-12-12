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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
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
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf<CaptureMode>(CaptureMode.Selection) }
    val context = LocalContext.current
    
    // Camera Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            mode = CaptureMode.Camera
        } else {
            // Handle denied - show snackbar or message? 
            // For now stay in selection
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
            // Resize if too big to avoid OOM or slow AI
            // For MVP, just pass it
            viewModel.capturedBitmap = bitmap
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                viewModel.analysisResult != null -> {
                    ResultView(viewModel, onDismiss)
                }
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
                        CaptureOption(
                            icon = R.drawable.ic_app_icon, // Fallback icon, usually camera icon
                            label = "Camera",
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    mode = CaptureMode.Camera
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        )
                        CaptureOption(
                            icon = R.drawable.ic_app_icon, // Fallback
                            label = "Gallery",
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        CaptureOption(
                            icon = R.drawable.ic_app_icon, // Fallback
                            label = "Text",
                            onClick = { mode = CaptureMode.Text }
                        )
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
                mode == CaptureMode.Camera -> {
                    CameraView(
                        onImageCaptured = { bmp -> 
                            viewModel.capturedBitmap = bmp
                        }
                    )
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
fun CaptureOption(icon: Int, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue)
        ) {
             // Basic text for now as we don't have all icons imported
             Text(text = label.take(1), style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

// ... existing CameraView (keep as is or minimal mod) ...
@Composable
fun CameraView(onImageCaptured: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    
    Box(Modifier.height(400.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))) {
        CameraPreview(imageCapture = imageCapture)
        Button(
            onClick = {
                imageCapture.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val buffer: ByteBuffer = image.planes[0].buffer
                            val bytes = ByteArray(buffer.remaining())
                            buffer.get(bytes)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            // Rotate if needed? usually Bitmap from byte array might lose EXIF
                            // For MVP scope, we assume it's okay or Gemini handles it.
                            onImageCaptured(bitmap)
                            image.close()
                        }
                        override fun onError(exception: ImageCaptureException) {
                            // Handle error
                        }
                    }
                )
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EnergeticOrange)
        ) {
            Text("Capture Photo")
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

@Composable
fun ResultView(viewModel: CaptureViewModel, onDismiss: () -> Unit) {
    val result = viewModel.analysisResult!!
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Result", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text(result.foodName, style = MaterialTheme.typography.titleLarge)
        Text("${result.calories} kcal", style = MaterialTheme.typography.displayMedium, color = DeepBlue)
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text("${result.protein}g C")
            Text("${result.carbs}g P")
            Text("${result.fat}g F")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { 
                viewModel.saveMeal()
                onDismiss() // Close sheet
            },
            colors = ButtonDefaults.buttonColors(containerColor = EnergeticOrange),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Log")
        }
    }
}

enum class CaptureMode { Selection, Camera, Text }
