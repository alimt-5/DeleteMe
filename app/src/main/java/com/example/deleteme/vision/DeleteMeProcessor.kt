package com.example.deleteme.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

class DeleteMeProcessor(
    context: Context,
    fps: Int = 30,
    registerForSeconds: Float = 0.2f,
) {

    private val handLandmarker =
        HandLandmarkerHelper(
            context
        )

    private val personDetector =
        PersonDetector(
            context
        )

    private val keepState =
        KeepState(
            keepForSeconds =
            registerForSeconds,
            fps =
            fps,
            initialState =
            false
        )

    private val mlFrameResizer =
        MlFrameResizer(
            targetWidth = 640,
            targetHeight = 360
        )

    private var backgroundBitmap:
            Bitmap? = null

    private var backgroundWidth =
        0

    private var backgroundHeight =
        0

    fun loadBackground(
        bitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int
    ) {

        backgroundBitmap?.recycle()

        backgroundBitmap =
            if (
                bitmap.width == targetWidth &&
                bitmap.height == targetHeight
            ) {

                bitmap.copy(
                    Bitmap.Config.ARGB_8888,
                    false
                )

            } else {

                Bitmap.createScaledBitmap(
                    bitmap,
                    targetWidth,
                    targetHeight,
                    true
                )
            }

        backgroundWidth =
            targetWidth

        backgroundHeight =
            targetHeight
    }

    fun process(
        frame: Bitmap
    ): Bitmap {

        val background =
            backgroundBitmap
                ?: return frame

        if (
            frame.width != backgroundWidth ||
            frame.height != backgroundHeight
        ) {
            return frame
        }

        val mlFrame =
            mlFrameResizer.resize(
                frame
            )

        try {

            val handResult =
                handLandmarker.detect(
                    mlFrame
                )

            val rawHandClosed =
                when (
                    handResult.state
                ) {

                    HandState.CLOSED ->
                        true

                    HandState.OPEN ->
                        false

                    HandState.NOT_DETECTED ->
                        false
                }

            val handClosed =
                keepState.update(
                    rawHandClosed
                )

            if (!handClosed) {
                return frame
            }

            val mlPersonBox =
                personDetector
                    .detectBoundingBox(
                        mlFrame
                    )
                    ?: return frame

            val personBox =
                mapPersonBoxToOriginalFrame(
                    box =
                    mlPersonBox,

                    originalWidth =
                    frame.width,

                    originalHeight =
                    frame.height,

                    mlWidth =
                    mlFrame.width,

                    mlHeight =
                    mlFrame.height
                )

            val output =
                frame.copy(
                    Bitmap.Config.ARGB_8888,
                    true
                )
            val canvas =
                Canvas(
                    output
                )

            val sourceRect =
                Rect(
                    personBox.x,
                    personBox.y,
                    personBox.x +
                            personBox.width,
                    personBox.y +
                            personBox.height
                )

            val destinationRect =
                Rect(
                    personBox.x,
                    personBox.y,
                    personBox.x +
                            personBox.width,
                    personBox.y +
                            personBox.height
                )

            canvas.drawBitmap(
                background,
                sourceRect,
                destinationRect,
                null
            )

            return output

        } finally {
            if (mlFrame !== frame) {
                mlFrame.recycle()
            }
        }
    }
    private fun mapPersonBoxToOriginalFrame(
        box: PersonBoundingBox,
        originalWidth: Int,
        originalHeight: Int,
        mlWidth: Int,
        mlHeight: Int
    ): PersonBoundingBox {

        val scaleX =
            originalWidth.toFloat() /
                    mlWidth.toFloat()

        val scaleY =
            originalHeight.toFloat() /
                    mlHeight.toFloat()

        val x =
            (
                    box.x *
                            scaleX
                    ).toInt()

        val y =
            (
                    box.y *
                            scaleY
                    ).toInt()

        val width =
            (
                    box.width *
                            scaleX
                    ).toInt()

        val height =
            (box.height * scaleY).toInt()

        val safeX =
            x.coerceIn(
                0,
                originalWidth
            )

        val safeY =
            y.coerceIn(
                0,
                originalHeight
            )
        val safeWidth =
            width.coerceAtMost(
                originalWidth -
                        safeX
            )

        val safeHeight =
            height.coerceAtMost(
                originalHeight -
                        safeY
            )

        if (
            safeWidth <= 0 ||
            safeHeight <= 0
        ) {

            return PersonBoundingBox(
                x = safeX,
                y = safeY,
                width = 1,
                height = 1
            )
        }

        return PersonBoundingBox(
            x = safeX,
            y = safeY,
            width = safeWidth,
            height = safeHeight
        )
    }

    fun resetState() {

        keepState.reset(
            initialState = false
        )
    }

    fun close() {

        handLandmarker.close()

        personDetector.close()

        backgroundBitmap?.recycle()

        backgroundBitmap = null

        backgroundWidth = 0

        backgroundHeight = 0
    }
}
