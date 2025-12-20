package ch.sebpiller.easy.lotto.ui

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

/**
 * A reusable component that lets the user:
 * 1) Open the camera viewfinder
 * 2) Take a photo
 * 3) Crop the captured photo with a rectangular selector
 * 4) Return the cropped [Bitmap] via [onResult]
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TakePhotoCropper(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onResult: (Bitmap) -> Unit,
) {
    val context = LocalContext.current
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

    var previewView: PreviewView? by remember { mutableStateOf(null) }
    val controller = remember {
        LifecycleCameraController(context).apply {
            // Default is back camera and image capture + preview
            setEnabledUseCases(
                LifecycleCameraController.IMAGE_CAPTURE or LifecycleCameraController.VIDEO_CAPTURE or LifecycleCameraController.IMAGE_ANALYSIS
            )
        }
    }

    var captured by remember { mutableStateOf<Bitmap?>(null) }
    var useFrontCamera by remember { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxSize()) {
        if (captured == null) {
            // Camera stage
            Column(Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Take a picture") },
                    navigationIcon = {
                        TextButton(onClick = onCancel) { Text("Cancel") }
                    },
                    actions = {
                        // Toggle front/back camera if available
                        TextButton(onClick = {
                            useFrontCamera = !useFrontCamera
                            controller.cameraSelector = if (useFrontCamera)
                                CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                        }) { Text(if (useFrontCamera) "Back" else "Front") }
                    }
                )

                if (cameraPermission.status is PermissionStatus.Granted) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                PreviewView(ctx).also { pv ->
                                    controller.cameraSelector = if (useFrontCamera)
                                        CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                                    pv.controller = controller
                                    previewView = pv
                                }
                            },
                            update = { pv ->
                                controller.cameraSelector = if (useFrontCamera)
                                    CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                                pv.controller = controller
                            }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onCancel) { Text("Close") }
                        Button(onClick = {
                            // Simplest capture path: grab current preview bitmap frame
                            // Note: PreviewView.getBitmap() may return null depending on impl
                            val bmp = previewView?.bitmap
                            if (bmp != null) {
                                captured = bmp
                            }
                        }) { Text("Capture") }
                    }
                } else {
                    // Ask for permission
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Camera permission is required")
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                                Text("Grant permission")
                            }
                        }
                    }
                }
            }
        } else {
            // Crop stage
            CropStage(
                bitmap = captured!!,
                onBack = { captured = null },
                onConfirm = { cropped ->
                    onResult(cropped)
                }
            )
        }
    }

    // Attach lifecycle to controller
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(controller, lifecycleOwner, useFrontCamera) {
        controller.bindToLifecycle(lifecycleOwner)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CropStage(
    bitmap: Bitmap,
    onBack: () -> Unit,
    onConfirm: (Bitmap) -> Unit,
) {
    var containerSize by remember { mutableStateOf(Size.Zero) }

    // Crop values normalized [0, 1]
    var normLeft by remember { mutableFloatStateOf(0.1f) }
    var normTop by remember { mutableFloatStateOf(0.1f) }
    var normRight by remember { mutableFloatStateOf(0.9f) }
    var normBottom by remember { mutableFloatStateOf(0.9f) }

    // Rotation state in degrees
    var rotation by remember { mutableIntStateOf(0) }

    // Compute rotated bitmap when rotation changes
    val displayBitmap by remember(bitmap, rotation) {
        mutableStateOf(
            when (rotation % 360 + 360 % 360) {
                0 -> rotateBitmap(bitmap, 0)
                90 -> rotateBitmap(bitmap, 90)
                180 -> rotateBitmap(bitmap, 180)
                270 -> rotateBitmap(bitmap, 270)
                else -> bitmap
            }
        )
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Crop image") },
            navigationIcon = {
                TextButton(onClick = onBack) { Text("Back") }
            },
            actions = {
                TextButton(onClick = { rotation = (rotation + 90) % 360 }) {
                    Text("Rotate")
                }
                TextButton(onClick = {
                    val displayW = containerSize.width
                    val displayH = containerSize.height
                    if (displayW <= 0f || displayH <= 0f) return@TextButton

                    val fit = fitCenter(displayBitmap.width.toFloat(), displayBitmap.height.toFloat(), displayW, displayH)
                    val scale = displayBitmap.width / fit.width
                    val imageLeft = (displayW - fit.width) / 2f
                    val imageTop = (displayH - fit.height) / 2f

                    val outLeft = (((normLeft * displayW - imageLeft) * scale).toInt()).coerceIn(0, displayBitmap.width - 1)
                    val outTop = (((normTop * displayW - imageTop) * scale).toInt()).coerceIn(0, displayBitmap.height - 1)
                    val outRight = (((normRight * displayW - imageLeft) * scale).toInt()).coerceIn(outLeft + 1, displayBitmap.width)
                    val outBottom = (((normBottom * displayW - imageTop) * scale).toInt()).coerceIn(outTop + 1, displayBitmap.height)

                    onConfirm(cropBitmap(displayBitmap, Rect(outLeft, outTop, outRight, outBottom)))
                }) { Text("Done") }
            }
        )

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            // Sliders for cropping
            CropSlider(label = "Left", value = normLeft, onValueChange = { normLeft = it.coerceAtMost(normRight - 0.05f) })
            CropSlider(label = "Right", value = normRight, onValueChange = { normRight = it.coerceAtLeast(normLeft + 0.05f) })
            CropSlider(label = "Top", value = normTop, onValueChange = { normTop = it.coerceAtMost(normBottom - 0.05f) })
            CropSlider(label = "Bottom", value = normBottom, onValueChange = { normBottom = it.coerceAtLeast(normTop + 0.05f) })

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                // Displayed image area fits inside the Box while preserving aspect ratio
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = displayBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { layoutCoordinates ->
                                val s = layoutCoordinates.size
                                containerSize = Size(s.width.toFloat(), s.height.toFloat())
                            }
                    )

                    CropOverlay(
                        normLeft = normLeft,
                        normTop = normTop,
                        normRight = normRight,
                        normBottom = normBottom
                    )
                }
            }
        }
    }
}

@Composable
private fun CropSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(60.dp), style = MaterialTheme.typography.bodySmall)
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            valueRange = 0f..1f
        )
    }
}

@Composable
private fun CropOverlay(
    normLeft: Float,
    normTop: Float,
    normRight: Float,
    normBottom: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val left = normLeft * w
        val top = normTop * h
        val right = normRight * w
        val bottom = normBottom * h

        // Darken outside
        drawRect(color = Color.Black.copy(alpha = 0.5f))
        
        // Highlight crop area
        drawRect(
            color = Color.White.copy(alpha = 0.12f),
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top)
        )
        // Border
        drawRect(
            color = Color(0xFF64B5F6),
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            style = Stroke(width = 3f)
        )
    }
}

private data class FitResult(val width: Float, val height: Float)

private fun fitCenter(srcW: Float, srcH: Float, dstW: Float, dstH: Float): FitResult {
    val scale = minOf(dstW / srcW, dstH / srcH)
    return FitResult(srcW * scale, srcH * scale)
}

private fun cropBitmap(src: Bitmap, rect: Rect): Bitmap {
    val left = rect.left.coerceIn(0, src.width - 1)
    val top = rect.top.coerceIn(0, src.height - 1)
    val width = (rect.width()).coerceIn(1, src.width - left)
    val height = (rect.height()).coerceIn(1, src.height - top)
    return Bitmap.createBitmap(src, left, top, width, height)
}

private fun rotateBitmap(src: Bitmap, degrees: Int): Bitmap {
    val d = ((degrees % 360) + 360) % 360
    if (d == 0) return src
    val matrix = Matrix().apply { postRotate(d.toFloat()) }
    return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
}

@Composable
fun TakePhotoCropperDialog(
    onDismissRequest: () -> Unit,
    onResult: (Bitmap) -> Unit,
) {
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    if (previewBitmap == null) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {},
            dismissButton = {},
            text = {
                Box(Modifier.fillMaxWidth().height(500.dp)) {
                    TakePhotoCropper(
                        modifier = Modifier.fillMaxSize(),
                        onCancel = onDismissRequest,
                        onResult = { bmp ->
                            previewBitmap = bmp
                        }
                    )
                }
            },
            title = {}
        )
    } else {
        TakePhotoResultDialog(
            bitmap = previewBitmap!!,
            onUse = {
                onResult(previewBitmap!!)
                onDismissRequest()
            },
            onClose = onDismissRequest
        )
    }
}

@Composable
private fun TakePhotoResultDialog(
    bitmap: Bitmap,
    onUse: () -> Unit,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = onUse) { Text("Use") }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text("Close") }
        },
        text = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Preview", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        },
        title = {}
    )
}

@Preview
@Composable
private fun TakePhotoResultDialogPreview() {
    TakePhotoResultDialog(
        bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888),
        onUse = {},
        onClose = {}
    )
}
