package com.example.deleteme.visions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

class DeleteMeProcessor(
    context: Context,
    fps: Int = 30,
    registerForSeconds: Float = 0.2f,
    private var mlBackgroundBitmap: Bitmap? = null,
    private var mlBackgroundWidth: Int = 0,
    private var mlBackgroundHeight: Int = 0
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

    /*
     * ML inference resolution.
     *
     * The camera/output frame keeps its original
     * resolution. Only ML receives this smaller frame.
     */
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

        /*
         * The background and camera frame must have
         * exactly the same dimensions.
         */
        if (
            frame.width != backgroundWidth ||
            frame.height != backgroundHeight
        ) {
            return frame
        }

        /*
         * --------------------------------------------------
         * Create a small frame ONLY for ML inference.
         * --------------------------------------------------
         *
         * Example:
         *
         * Original:
         * 1920 x 1080
         *
         * ML:
         * 640 x 360
         */
        val mlFrame =
            mlFrameResizer.resize(
                frame
            )

        try {

            /*
             * --------------------------------------------------
             * 1. Hand detection
             * --------------------------------------------------
             */
            val handResult =
                handLandmarker.detect(
                    mlFrame
                )

            /*
             * Original behavior:
             *
             * CLOSED -> true
             * OPEN -> false
             * NOT_DETECTED -> false
             */
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

            /*
             * --------------------------------------------------
             * 2. Stabilize hand state
             * --------------------------------------------------
             */
            val handClosed =
                keepState.update(
                    rawHandClosed
                )

            /*
             * --------------------------------------------------
             * 3. If hand isn't closed,
             *    don't run person detection.
             * --------------------------------------------------
             */
            if (!handClosed) {
                return frame
            }

            /*
             * --------------------------------------------------
             * 4. Detect person on the SMALL ML frame.
             * --------------------------------------------------
             */
            val mlPersonBox =
                personDetector
                    .detectBoundingBox(
                        mlFrame
                    )
                    ?: return frame

            /*
             * --------------------------------------------------
             * 5. Convert the bounding box from ML resolution
             *    back to the original camera resolution.
             * --------------------------------------------------
             */
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

            /*
             * --------------------------------------------------
             * 6. Create output using the ORIGINAL resolution.
             * --------------------------------------------------
             */
            val output =
                frame.copy(
                    Bitmap.Config.ARGB_8888,
                    true
                )

            /*
             * --------------------------------------------------
             * 7. Draw the background over the detected person.
             * --------------------------------------------------
             */
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

            /*
             * mlFrame is only used for inference.
             *
             * It is NOT displayed by Compose.
             *
             * Therefore it is safe to recycle here.
             */
            if (mlFrame !== frame) {
                mlFrame.recycle()
            }
        }
    }

    /*
     * Converts a bounding box from:
     *
     * 640 x 360
     *
     * back to:
     *
     * original camera resolution
     */
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
            (
                    box.height *
                            scaleY
                    ).toInt()

        /*
         * Clamp X and Y so that they stay inside
         * the original frame.
         */
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

        /*
         * Make sure width/height don't extend
         * outside the image.
         */
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
