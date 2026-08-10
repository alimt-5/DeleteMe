package com.example.deleteme.ui

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.deleteme.camera.CameraFrameAnalyzer
import com.example.deleteme.camera.toRotatedBitmap
import com.example.deleteme.visions.DeleteMeProcessor
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun BackgroundCaptureScreen(
    cameraPermissionGranted: Boolean,
    onRequestCameraPermission: () -> Unit
) {

    val context = LocalContext.current

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var currentFrame by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var backgroundBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var showBackgroundPreview by remember {
        mutableStateOf(false)
    }

    var isCapturingBackground by remember {
        mutableStateOf(false)
    }

    var backgroundCaptured by remember {
        mutableStateOf(false)
    }

    var countdown by remember {
        mutableIntStateOf(0)
    }

    var effectStarted by remember {
        mutableStateOf(false)
    }

    var lensFacing by remember {
        mutableIntStateOf(CameraSelector.LENS_FACING_FRONT)
    }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    val processor = remember {
        DeleteMeProcessor(
            context = context,
            fps = 30,
            registerForSeconds = 0.2f
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            processor.close()
            analysisExecutor.shutdown()
            backgroundBitmap?.recycle()
        }
    }
    if (!cameraPermissionGranted) {
        PermissionScreen(
            onRequestPermission =
            onRequestCameraPermission
        )
        return
    }
    CameraAnalysis(
        lifecycleOwner = lifecycleOwner,
        analysisExecutor = analysisExecutor,
        processor = processor,
        effectStarted = effectStarted,
        lensFacing = lensFacing,
        onFrame = { bitmap ->
            currentFrame = bitmap
        }
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        currentFrame?.let { bitmap ->

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        if (showBackgroundPreview && !effectStarted) {
            backgroundBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier =
                    Modifier
                        .fillMaxSize(),

                    contentScale =
                    ContentScale.Crop
                )
            }
        }
        Button(
            onClick = {

                lensFacing =
                    if (
                        lensFacing ==
                        CameraSelector.LENS_FACING_FRONT
                    ) {
                        CameraSelector.LENS_FACING_BACK
                    } else {
                        CameraSelector.LENS_FACING_FRONT
                    }
            },

            modifier =
            Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        ) {

            Text(
                text = "Switch Camera"
            )
        }

        /*
         * Before effect starts,
         * show capture controls.
         */
        if (!effectStarted) {

            if (!isCapturingBackground) {

                CaptureBackgroundControls(
                    backgroundCaptured =
                    backgroundCaptured,

                    onCaptureClick = {

                        isCapturingBackground =
                            true

                        countdown =
                            5
                    },

                    onStartClick = {

                        val frame =
                            backgroundBitmap

                        val width =
                            frame?.width

                        val height =
                            frame?.height

                        if (
                            frame != null &&
                            width != null &&
                            height != null
                        ) {

                            processor.loadBackground(
                                bitmap = frame,
                                targetWidth = width,
                                targetHeight = height
                            )

                            processor.resetState()

                            effectStarted =
                                true
                        }
                    }
                )

            } else {

                CountdownOverlay(
                    countdown =
                    countdown
                )
            }
        }
    }

    /*
     * Background capture countdown.
     */
    LaunchedEffect(
        isCapturingBackground
    ) {

        if (!isCapturingBackground) {
            return@LaunchedEffect
        }

        for (
        second in 5 downTo 1
        ) {

            countdown =
                second

            delay(1000)
        }

        countdown = 0

        val frame =
            currentFrame

        if (frame != null) {

            backgroundBitmap?.recycle()

            backgroundBitmap =
                frame.copy(
                    Bitmap.Config.ARGB_8888,
                    false
                )

            backgroundCaptured =
                true
            showBackgroundPreview =
                true
        }

        isCapturingBackground =
            false
    }
}


@Composable
private fun CameraAnalysis(
    lifecycleOwner: LifecycleOwner,
    analysisExecutor: ExecutorService,
    processor: DeleteMeProcessor,
    effectStarted: Boolean,
    lensFacing: Int,
    onFrame: (Bitmap) -> Unit
) {

    val context =
        LocalContext.current

    DisposableEffect(
        lifecycleOwner,
        effectStarted,
        lensFacing
    ) {

        val cameraProviderFuture =
            ProcessCameraProvider
                .getInstance(context)

        val executor =
            ContextCompat.getMainExecutor(
                context
            )

        var cameraProvider:
                ProcessCameraProvider? = null

        cameraProviderFuture.addListener({

            try {

                cameraProvider =
                    cameraProviderFuture.get()

                val imageAnalysis =
                    ImageAnalysis.Builder()
                        .setBackpressureStrategy(
                            ImageAnalysis
                                .STRATEGY_KEEP_ONLY_LATEST
                        )
                        .setOutputImageFormat(
                            ImageAnalysis
                                .OUTPUT_IMAGE_FORMAT_RGBA_8888
                        )
                        .build()

                val analyzer =
                    if (effectStarted) {

                        CameraFrameAnalyzer(
                            processor = processor,
                            onFrame = onFrame,
                            targetFps = 10
                        )

                    } else {

                        CameraPreviewAnalyzer(
                            onFrame = onFrame,
                        )
                    }

                imageAnalysis.setAnalyzer(
                    analysisExecutor,
                    analyzer
                )

                cameraProvider?.unbindAll()

                val cameraSelector =
                    CameraSelector.Builder()
                        .requireLensFacing(
                            lensFacing
                        )
                        .build()

                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageAnalysis
                )

            } catch (e: Exception) {

                e.printStackTrace()
            }

        }, executor)

        onDispose {

            try {

                cameraProvider?.unbindAll()

            } catch (
                ignored: Exception
            ) {
            }
        }
    }
}


private class CameraPreviewAnalyzer(
    private val onFrame: (Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(
        imageProxy:
        androidx.camera.core.ImageProxy
    ) {

        try {

            val bitmap =
                imageProxy.toRotatedBitmap()

            onFrame(
                bitmap
            )

        } catch (e: Exception) {

            e.printStackTrace()

        } finally {

            imageProxy.close()
        }
    }
}


@Composable
private fun CaptureBackgroundControls(
    backgroundCaptured: Boolean,
    onCaptureClick: () -> Unit,
    onStartClick: () -> Unit
) {

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(24.dp),

        contentAlignment =
        Alignment.BottomCenter
    ) {

        Column(
            modifier =
            Modifier.fillMaxWidth(),

            horizontalAlignment =
            Alignment.CenterHorizontally,

            verticalArrangement =
            Arrangement.Bottom
        ) {

            if (backgroundCaptured) {

                Text(
                    text =
                    "Background captured",

                    color =
                    Color.White,

                    style =
                    MaterialTheme.typography
                        .titleMedium
                )

                Spacer(
                    modifier =
                    Modifier.height(12.dp)
                )

                Button(
                    onClick =
                    onStartClick,

                    modifier =
                    Modifier.fillMaxWidth()
                ) {

                    Text(
                        text =
                        "Start DeleteMe"
                    )
                }

                Spacer(
                    modifier =
                    Modifier.height(8.dp)
                )
            }

            Button(
                onClick =
                onCaptureClick,

                modifier =
                Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                    if (backgroundCaptured) {
                        "Capture Again"
                    } else {
                        "Capture Background"
                    }
                )
            }
        }
    }
}


@Composable
private fun CountdownOverlay(
    countdown: Int
) {

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = 0.25f
                )
            ),

        contentAlignment =
        Alignment.Center
    ) {

        if (countdown > 0) {

            Text(
                text =
                countdown.toString(),

                color =
                Color.White,

                style =
                MaterialTheme.typography
                    .displayLarge
            )

        } else {

            CircularProgressIndicator(
                color =
                Color.White
            )
        }
    }
}


@Composable
private fun PermissionScreen(
    onRequestPermission: () -> Unit
) {

    Box(
        modifier =
        Modifier.fillMaxSize(),

        contentAlignment =
        Alignment.Center
    ) {

        Button(
            onClick =
            onRequestPermission
        ) {

            Text(
                text =
                "Grant Camera Permission"
            )
        }
    }
}