package ch.sebpiller.easy.lotto

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import ch.sebpiller.easy.lotto.ocr.LottoGridRecognizer
import ch.sebpiller.easy.lotto.ui.theme.EasyLottoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate called")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyLottoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CameraCaptureScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraCaptureScreen() {
    Log.d("CameraCaptureScreen", "Composable initialized")
    val context = LocalContext.current
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    var lastSavedUri by remember { mutableStateOf<Uri?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (!cameraPermission.status.isGranted) {
            LaunchedEffect(Unit) { cameraPermission.launchPermissionRequest() }
            PermissionRationale(modifier = Modifier.padding(padding)) {
                cameraPermission.launchPermissionRequest()
            }
        } else {
            val scope = rememberCoroutineScope()

            val controller = remember { LifecycleCameraController(context) }
            val lifecycleOwner = LocalLifecycleOwner.current
            LaunchedEffect(lifecycleOwner) {
                controller.imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                controller.bindToLifecycle(lifecycleOwner)
            }

            val executor: Executor = ContextCompat.getMainExecutor(context)

            // Left panel state
            var toggleOn by remember { mutableStateOf(false) }
            var radioSelected by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black)
            ) {
                // Left panel
                Column(
                    modifier = Modifier
                        .width(200.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF101010))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Controls", color = Color.White)
                    Spacer(Modifier.height(12.dp))

                    // Toggle (Switch)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Switch(checked = toggleOn, onCheckedChange = { toggleOn = it })
                        Spacer(Modifier.width(8.dp))
                        Text("Toggle", color = Color.White)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Radio button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(selected = radioSelected, onClick = { radioSelected = !radioSelected })
                        Spacer(Modifier.width(8.dp))
                        Text("Option A", color = Color.White)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Action button
                    Button(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Left action clicked • toggle=$toggleOn • radio=$radioSelected")
                        }
                    }) {
                        Text("Left Action")
                    }
                }

                // Right content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AndroidView(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                this.controller = controller
                            }
                        },
                        update = { it.controller = controller }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            captureAndSavePhoto(context, controller, executor) { result ->
                                lastSavedUri = result
                                val msg = if (result != null) {
                                    "Saved: $result"
                                } else "Capture failed"

                                scope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        }) {
                            Text("Take photo")
                        }

                        lastSavedUri?.let { uri ->
                            OutlinedButton(onClick = { processLast(context, uri) }) {
                                Text("Import")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRationale(modifier: Modifier = Modifier, onRequest: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Camera permission is required to take photos.")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRequest) { Text("Grant permission") }
    }
}

private fun captureAndSavePhoto(
    context: Context,
    controller: LifecycleCameraController,
    executor: Executor,
    onResult: (Uri?) -> Unit
) {
    Log.d("CapturePhoto", "Starting photo capture")
    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val outputOptions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/EasyLotto")
            }


            // Utilisation du constructeur qui prend la Collection URI et laisse CameraX gérer l'insertion
            ImageCapture.OutputFileOptions.Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build()
        }

        else -> {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "EasyLotto").apply { mkdirs() }
            val file = File(dir, "$name.jpg")
            ImageCapture.OutputFileOptions.Builder(file).build()
        }
    }

    controller.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onError(exception: ImageCaptureException) {
            Log.e("CapturePhoto", "Error during photo capture", exception)
            onResult(null)
        }

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val savedUri = outputFileResults.savedUri ?: outputOptions.savedUriFromLegacy(context)
            onResult(savedUri)
        }
    })
}

// Helper to get Uri when saving to a File on pre-Q
private fun ImageCapture.OutputFileOptions.savedUriFromLegacy(context: Context): Uri? {
    try {
        val javaClass = this.javaClass
        val fileField = javaClass.getDeclaredField("mFile")
        fileField.isAccessible = true
        val file = fileField[this] as? File ?: return null
        // Add to MediaStore so it appears in gallery
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.DATA, file.absolutePath)
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    } catch (_: Throwable) {
        return null
    }
}

private fun processLast(context: Context, uri: Uri) {
    Log.d("ProcessLast", "Processing: $uri")
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val reader = LottoGridRecognizer()
            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            val image = InputImage.fromBitmap(bitmap, 0)

            val detected = reader.extractAllNumbers(image)

            // Sort by row then column for stable output
            val sorted = detected.sortedWith(compareBy({ it.position.row }, { it.position.col }))

            for (d in sorted) {
                Log.i(
                    "LottoExtract",
                    "value=${d.value} at row=${d.position.row}, col=${d.position.col}"
                )
            }

            val summary = if (sorted.isEmpty()) {
                "No numbers detected"
            } else {
                val items = sorted.joinToString { "${it.value}@(${it.position.row},${it.position.col})" }
                "Detected ${sorted.size}: $items"
            }

            Toast.makeText(context, summary, Toast.LENGTH_LONG).show()

        } catch (t: Throwable) {
            Log.e("ProcessLast", "Failed to process image", t)
            Toast.makeText(context, "Processing failed: ${t.message}", Toast.LENGTH_LONG).show()

        }
    }
}
