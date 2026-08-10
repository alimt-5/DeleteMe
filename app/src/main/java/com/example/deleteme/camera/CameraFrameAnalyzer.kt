package com.example.deleteme.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.deleteme.vision.DeleteMeProcessor
import java.util.concurrent.atomic.AtomicBoolean

class CameraFrameAnalyzer(
    private val processor: DeleteMeProcessor,
    private val onFrame: (Bitmap) -> Unit,
    targetFps: Int = 10
) : ImageAnalysis.Analyzer {

    private val isProcessing = AtomicBoolean(false)
    private val frameRateLimiter = FrameRateLimiter(targetFps = targetFps)

    override fun analyze(imageProxy: ImageProxy) {
        if (!frameRateLimiter.shouldProcess()) {
            imageProxy.close()
            return
        }
        if (!isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }
        var bitmap: Bitmap? = null
        var processedBitmap: Bitmap? = null

        try {
            bitmap = imageProxy.toRotatedBitmap()
            processedBitmap = processor.process(bitmap)
            onFrame(processedBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
            isProcessing.set(false)
        }
    }
}