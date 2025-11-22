package ch.sebpiller.easy.lotto

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import ch.sebpiller.easy.lotto.ui.theme.EasyLottoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
            val controller = remember { LifecycleCameraController(context) }
            val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
            LaunchedEffect(lifecycleOwner) {
                controller.imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                controller.bindToLifecycle(lifecycleOwner)
            }

            val executor: Executor = ContextCompat.getMainExecutor(context)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black),
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
                            } else "Capture failed";

                            // FIXME
//                            LaunchedEffect(msg) {
//                                snackbarHostState.showSnackbar(msg)
//                            }
                        }
                    }) {
                        Text("Take photo")
                    }

                    lastSavedUri?.let { uri ->
                        OutlinedButton(onClick = { shareImage(context, uri) }) {
                            Text("Share last")
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
    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val outputOptions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/EasyLotto")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri == null) {
                onResult(null)
                return
            }
            ImageCapture.OutputFileOptions.Builder(context.contentResolver, uri, ContentValues()).build()
        }

        else -> {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "EasyLotto").apply { mkdirs() }
            val file = File(dir, "$name.jpg")
            ImageCapture.OutputFileOptions.Builder(file).build()
        }
    }

    controller.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onError(exception: ImageCaptureException) {
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
        val file = fileField.get(this) as? File ?: return null
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

private fun shareImage(context: Context, uri: Uri) {
    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share image"))
}
