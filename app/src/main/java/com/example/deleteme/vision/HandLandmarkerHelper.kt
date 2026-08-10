package com.example.deleteme.vision

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class HandLandmarkerHelper(
    context: Context
) {

    companion object {

        private const val MODEL_NAME =
            "hand_landmarker.task"

        private const val NUM_HANDS =
            1

        private const val MIN_HAND_DETECTION_CONFIDENCE =
            0.5f

        private const val MIN_TRACKING_CONFIDENCE =
            0.5f
    }

    private val handLandmarker: HandLandmarker

    init {

        val baseOptions =
            BaseOptions.builder()
                .setModelAssetPath(
                    MODEL_NAME
                )
                .build()

        val options =
            HandLandmarker.HandLandmarkerOptions
                .builder()
                .setBaseOptions(
                    baseOptions
                )
                .setRunningMode(
                    RunningMode.IMAGE
                )
                .setNumHands(
                    NUM_HANDS
                )
                .setMinHandDetectionConfidence(
                    MIN_HAND_DETECTION_CONFIDENCE
                )
                .setMinTrackingConfidence(
                    MIN_TRACKING_CONFIDENCE
                )
                .build()

        handLandmarker =
            HandLandmarker.createFromOptions(
                context,
                options
            )
    }

    fun detect(
        bitmap: Bitmap
    ): HandDetectionResult {

        val mpImage: MPImage =
            BitmapImageBuilder(
                bitmap
            ).build()

        val result =
            handLandmarker.detect(
                mpImage
            )

        return convertResult(result)
    }

    private fun convertResult(
        result: HandLandmarkerResult
    ): HandDetectionResult {

        if (result.landmarks().isEmpty()) {

            return HandDetectionResult(
                state =
                HandState.NOT_DETECTED,
                extendedFingerCount = 0,
                landmarks = emptyList()
            )
        }

        val firstHand =
            result.landmarks()[0]

        val landmarks =
            firstHand.map { landmark ->

                HandLandmarkPoint(
                    x = landmark.x(),
                    y = landmark.y(),
                    z = landmark.z()
                )
            }

        val classifier =
            FistClassifier()

        val classification =
            classifier.classify(
                landmarks
            )

        return HandDetectionResult(
            state =
            classification.state,
            extendedFingerCount =
            classification.extendedFingerCount,
            landmarks =
            landmarks
        )
    }

    fun close() {

        handLandmarker.close()
    }
}