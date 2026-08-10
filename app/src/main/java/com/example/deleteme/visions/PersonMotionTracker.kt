package com.example.deleteme.visions

import kotlin.math.roundToInt

class PersonMotionTracker(
    private val positionSmoothing: Float = 0.65f,
    private val velocitySmoothing: Float = 0.55f,
    private val maxPredictionFrames: Int = 15
) {

    private var trackedBox: PersonBoundingBox? = null

    private var velocityX = 0f
    private var velocityY = 0f

    private var velocityWidth = 0f
    private var velocityHeight = 0f

    private var lostFrames = 0

    fun update(
        detectedBox: PersonBoundingBox?
    ): PersonBoundingBox? {

        /*
         * ==================================================
         * FIRST DETECTION
         * ==================================================
         */

        if (trackedBox == null) {

            if (detectedBox == null) {
                return null
            }

            trackedBox =
                detectedBox

            lostFrames = 0

            velocityX = 0f
            velocityY = 0f
            velocityWidth = 0f
            velocityHeight = 0f

            return trackedBox
        }


        /*
         * ==================================================
         * NEW DETECTION
         * ==================================================
         */

        if (detectedBox != null) {

            val current =
                trackedBox!!

            /*
             * Calculate difference between the
             * tracked position and the new detection.
             */

            val dx =
                detectedBox.x -
                        current.x

            val dy =
                detectedBox.y -
                        current.y

            val dw =
                detectedBox.width -
                        current.width

            val dh =
                detectedBox.height -
                        current.height


            /*
             * Ignore extremely large jumps.
             *
             * This prevents a bad detection from
             * suddenly moving the replacement area
             * across the screen.
             */

            val maxJumpX =
                current.width * 1.5f

            val maxJumpY =
                current.height * 1.5f


            val safeDx =
                dx.coerceIn(
                    -maxJumpX.toInt(),
                    maxJumpX.toInt()
                )

            val safeDy =
                dy.coerceIn(
                    -maxJumpY.toInt(),
                    maxJumpY.toInt()
                )


            /*
             * Update velocity.
             */

            velocityX =
                velocityX *
                        (1f - velocitySmoothing) +
                        safeDx *
                        velocitySmoothing

            velocityY =
                velocityY *
                        (1f - velocitySmoothing) +
                        safeDy *
                        velocitySmoothing

            velocityWidth =
                velocityWidth *
                        (1f - velocitySmoothing) +
                        dw *
                        velocitySmoothing

            velocityHeight =
                velocityHeight *
                        (1f - velocitySmoothing) +
                        dh *
                        velocitySmoothing


            /*
             * Smooth the new position.
             */

            val newX =
                (
                        current.x +
                                safeDx *
                                positionSmoothing
                        ).roundToInt()

            val newY =
                (
                        current.y +
                                safeDy *
                                positionSmoothing
                        ).roundToInt()

            val newWidth =
                (
                        current.width +
                                dw *
                                positionSmoothing
                        ).roundToInt()

            val newHeight =
                (
                        current.height +
                                dh *
                                positionSmoothing
                        ).roundToInt()


            trackedBox =
                PersonBoundingBox(
                    x = newX,
                    y = newY,
                    width =
                    newWidth.coerceAtLeast(1),
                    height =
                    newHeight.coerceAtLeast(1)
                )

            lostFrames = 0

            return trackedBox
        }


        /*
         * ==================================================
         * DETECTION LOST
         * ==================================================
         */

        lostFrames++

        if (
            lostFrames >
            maxPredictionFrames
        ) {

            reset()

            return null
        }


        /*
         * Predict the next position using
         * the current velocity.
         */

        val current =
            trackedBox
                ?: return null


        val predictedX =
            (
                    current.x +
                            velocityX
                    ).roundToInt()

        val predictedY =
            (
                    current.y +
                            velocityY
                    ).roundToInt()

        val predictedWidth =
            (
                    current.width +
                            velocityWidth
                    ).roundToInt()

        val predictedHeight =
            (
                    current.height +
                            velocityHeight
                    ).roundToInt()


        trackedBox =
            PersonBoundingBox(
                x = predictedX,
                y = predictedY,
                width =
                predictedWidth.coerceAtLeast(1),
                height =
                predictedHeight.coerceAtLeast(1)
            )

        /*
         * Gradually reduce velocity while detection
         * is unavailable.
         *
         * This prevents unlimited acceleration.
         */

        velocityX *= 0.90f
        velocityY *= 0.90f

        velocityWidth *= 0.90f
        velocityHeight *= 0.90f

        return trackedBox
    }


    fun current():
            PersonBoundingBox? {

        return trackedBox
    }


    fun isTracking():
            Boolean {

        return trackedBox != null
    }


    fun lostFrameCount():
            Int {

        return lostFrames
    }


    fun reset() {

        trackedBox = null

        velocityX = 0f
        velocityY = 0f

        velocityWidth = 0f
        velocityHeight = 0f

        lostFrames = 0
    }
}