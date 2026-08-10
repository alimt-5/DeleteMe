//package com.example.deleteme.visions
//
//import kotlin.math.roundToInt
//
//class PersonDetectionMemory(
//    private val maxLostFrames: Int = 15
//) {
//
//    private var lastBox: PersonBoundingBox? = null
//
//    private var previousDetectedBox:
//            PersonBoundingBox? = null
//
//    /*
//     * Estimated movement of the person between
//     * two successful detections.
//     */
//    private var velocityX = 0f
//    private var velocityY = 0f
//
//    private var velocityWidth = 0f
//    private var velocityHeight = 0f
//
//    private var lostFrames = 0
//
//    fun update(
//        detectedBox: PersonBoundingBox?
//    ): PersonBoundingBox? {
//
//        /*
//         * ==================================================
//         * NEW DETECTION
//         * ==================================================
//         */
//
//        if (detectedBox != null) {
//
//            val previous =
//                previousDetectedBox
//
//            if (previous != null) {
//
//                /*
//                 * Calculate movement of the box.
//                 */
//
//                val dx =
//                    detectedBox.x -
//                            previous.x
//
//                val dy =
//                    detectedBox.y -
//                            previous.y
//
//                val dw =
//                    detectedBox.width -
//                            previous.width
//
//                val dh =
//                    detectedBox.height -
//                            previous.height
//
//                /*
//                 * Smooth the velocity.
//                 *
//                 * We don't immediately trust one detection.
//                 */
//
//                velocityX =
//                    velocityX * 0.35f +
//                            dx * 0.65f
//
//                velocityY =
//                    velocityY * 0.35f +
//                            dy * 0.65f
//
//                velocityWidth =
//                    velocityWidth * 0.35f +
//                            dw * 0.65f
//
//                velocityHeight =
//                    velocityHeight * 0.35f +
//                            dh * 0.65f
//            }
//
//            /*
//             * Store current detection.
//             */
//
//            previousDetectedBox =
//                detectedBox
//
//            lastBox =
//                detectedBox
//
//            lostFrames =
//                0
//
//            return detectedBox
//        }
//
//
//        /*
//         * ==================================================
//         * DETECTION LOST
//         * ==================================================
//         */
//
//        val currentBox =
//            lastBox
//                ?: return null
//
//        lostFrames++
//
//        /*
//         * Too many lost frames.
//         */
//
//        if (
//            lostFrames >
//            maxLostFrames
//        ) {
//
//            lastBox =
//                null
//
//            previousDetectedBox =
//                null
//
//            velocityX =
//                0f
//
//            velocityY =
//                0f
//
//            velocityWidth =
//                0f
//
//            velocityHeight =
//                0f
//
//            return null
//        }
//
//
//        /*
//         * ==================================================
//         * PREDICT NEXT POSITION
//         * ==================================================
//         *
//         * Instead of keeping the box frozen,
//         * move it according to the estimated velocity.
//         */
//
//        val predictedX =
//            (
//                    currentBox.x +
//                            velocityX
//                    ).roundToInt()
//
//        val predictedY =
//            (
//                    currentBox.y +
//                            velocityY
//                    ).roundToInt()
//
//        val predictedWidth =
//            (
//                    currentBox.width +
//                            velocityWidth
//                    ).roundToInt()
//
//        val predictedHeight =
//            (
//                    currentBox.height +
//                            velocityHeight
//                    ).roundToInt()
//
//
//        /*
//         * Prevent the predicted box from becoming
//         * invalid.
//         */
//
//        val safeWidth =
//            predictedWidth.coerceAtLeast(
//                1
//            )
//
//        val safeHeight =
//            predictedHeight.coerceAtLeast(
//                1
//            )
//
//
//        val predictedBox =
//            PersonBoundingBox(
//                x = predictedX,
//                y = predictedY,
//                width = safeWidth,
//                height = safeHeight
//            )
//
//        /*
//         * Store prediction so the next lost frame
//         * continues moving from the predicted position.
//         */
//
//        lastBox =
//            predictedBox
//
//        return predictedBox
//    }
//
//
//    fun reset() {
//
//        lastBox =
//            null
//
//        previousDetectedBox =
//            null
//
//        velocityX =
//            0f
//
//        velocityY =
//            0f
//
//        velocityWidth =
//            0f
//
//        velocityHeight =
//            0f
//
//        lostFrames =
//            0
//    }
//
//
//    fun current():
//            PersonBoundingBox? {
//
//        return lastBox
//    }
//}
package com.example.deleteme.visions

import kotlin.math.roundToInt

class PersonDetectionMemory(
    private val maxLostFrames: Int = 8,
    private val smoothing: Float = 0.65f,
    private val predictionFactor: Float = 1.0f
) {

    private var lastBox: PersonBoundingBox? = null

    private var velocityX = 0f
    private var velocityY = 0f
    private var velocityWidth = 0f
    private var velocityHeight = 0f

    private var lostFrames = 0

    fun update(
        detectedBox: PersonBoundingBox?
    ): PersonBoundingBox? {

        /*
         * --------------------------------------------------
         * New detection
         * --------------------------------------------------
         */
        if (detectedBox != null) {

            val previous =
                lastBox

            if (previous == null) {

                lastBox =
                    detectedBox

                velocityX = 0f
                velocityY = 0f
                velocityWidth = 0f
                velocityHeight = 0f

                lostFrames = 0

                return detectedBox
            }

            /*
             * --------------------------------------------------
             * Calculate movement between the previous and
             * current detection.
             * --------------------------------------------------
             */
            val newVelocityX =
                (
                        detectedBox.x -
                                previous.x
                        ).toFloat()

            val newVelocityY =
                (
                        detectedBox.y -
                                previous.y
                        ).toFloat()

            val newVelocityWidth =
                (
                        detectedBox.width -
                                previous.width
                        ).toFloat()

            val newVelocityHeight =
                (
                        detectedBox.height -
                                previous.height
                        ).toFloat()

            /*
             * Smooth velocity.
             */
            velocityX =
                velocityX * 0.5f +
                        newVelocityX * 0.5f

            velocityY =
                velocityY * 0.5f +
                        newVelocityY * 0.5f

            velocityWidth =
                velocityWidth * 0.5f +
                        newVelocityWidth * 0.5f

            velocityHeight =
                velocityHeight * 0.5f +
                        newVelocityHeight * 0.5f

            /*
             * --------------------------------------------------
             * Smooth the detected box.
             * --------------------------------------------------
             */
            val smoothedX =
                (
                        previous.x +
                                (
                                        detectedBox.x -
                                                previous.x
                                        ) * smoothing
                        ).roundToInt()

            val smoothedY =
                (
                        previous.y +
                                (
                                        detectedBox.y -
                                                previous.y
                                        ) * smoothing
                        ).roundToInt()

            val smoothedWidth =
                (
                        previous.width +
                                (
                                        detectedBox.width -
                                                previous.width
                                        ) * smoothing
                        ).roundToInt()

            val smoothedHeight =
                (
                        previous.height +
                                (
                                        detectedBox.height -
                                                previous.height
                                        ) * smoothing
                        ).roundToInt()

            val result =
                PersonBoundingBox(
                    x = smoothedX,
                    y = smoothedY,
                    width = smoothedWidth,
                    height = smoothedHeight
                )

            lastBox =
                result

            lostFrames = 0

            return result
        }

        /*
         * --------------------------------------------------
         * Detection temporarily lost.
         * --------------------------------------------------
         */
        lostFrames++

        if (lastBox == null) {
            return null
        }

        if (lostFrames > maxLostFrames) {

            reset()

            return null
        }

        /*
         * --------------------------------------------------
         * Predict where the person should be.
         *
         * This is important when the user moves quickly.
         * --------------------------------------------------
         */
        val previous =
            lastBox!!

        val predictedX =
            (
                    previous.x +
                            velocityX * predictionFactor
                    ).roundToInt()

        val predictedY =
            (
                    previous.y +
                            velocityY * predictionFactor
                    ).roundToInt()

        val predictedWidth =
            (
                    previous.width +
                            velocityWidth * predictionFactor
                    ).roundToInt()

        val predictedHeight =
            (
                    previous.height +
                            velocityHeight * predictionFactor
                    ).roundToInt()

        val predicted =
            PersonBoundingBox(
                x = predictedX,
                y = predictedY,
                width = predictedWidth,
                height = predictedHeight
            )

        lastBox =
            predicted

        return predicted
    }

    private fun reset() {

        lastBox = null

        velocityX = 0f
        velocityY = 0f
        velocityWidth = 0f
        velocityHeight = 0f

        lostFrames = 0
    }

    fun current(): PersonBoundingBox? {
        return lastBox
    }
}