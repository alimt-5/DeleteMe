package com.example.deleteme.vision

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult

class PersonDetector(
    context: Context
) {

    companion object {

        private const val MODEL_NAME =
            "efficientdet_lite0.tflite"

        private const val SCORE_THRESHOLD =
            0.5f

        private const val PADDING =
            10
    }

    private val detector: ObjectDetector

    init {

        val baseOptions =
            BaseOptions.builder()
                .setModelAssetPath(
                    MODEL_NAME
                )
                .build()

        val options =
            ObjectDetector.ObjectDetectorOptions
                .builder()
                .setBaseOptions(
                    baseOptions
                )
                .setRunningMode(
                    RunningMode.IMAGE
                )
                .setScoreThreshold(
                    SCORE_THRESHOLD
                )
                .setCategoryAllowlist(
                    listOf("person")
                )
                .build()

        detector =
            ObjectDetector.createFromOptions(
                context,
                options
            )
    }

    fun detectBoundingBox(
        bitmap: Bitmap
    ): PersonBoundingBox? {

        val mpImage =
            BitmapImageBuilder(
                bitmap
            ).build()

        val result =
            detector.detect(
                mpImage
            )

        return findLargestPerson(
            result = result,
            imageWidth = bitmap.width,
            imageHeight = bitmap.height
        )
    }

    private fun findLargestPerson(
        result: ObjectDetectorResult,
        imageWidth: Int,
        imageHeight: Int
    ): PersonBoundingBox? {

        var largestArea = 0L
        var largestBox: PersonBoundingBox? = null

        for (detection in result.detections()) {
            val box = detection.boundingBox()
            val rawX = box.left
            val rawY = box.top
            val rawWidth = box.width()
            val rawHeight = box.height()

            val area = rawWidth.toLong() * rawHeight.toLong()
            if (area <= largestArea) {
                continue
            }
            largestArea = area

            val x = maxOf(0, (rawX - PADDING).toInt())
            val y = maxOf(0, (rawY - PADDING).toInt())

            val width = minOf(
                imageWidth - x,
                (rawWidth + PADDING * 2).toInt()
            )
            val height = minOf(
                imageHeight - y,
                (rawHeight + PADDING * 2).toInt()
            )

            if (width <= 0 || height <= 0) {
                continue
            }

            largestBox = PersonBoundingBox(x = x, y = y, width = width, height = height)
        }

        return largestBox
    }

    fun close() {

        detector.close()
    }
}