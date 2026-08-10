package com.example.deleteme.visions

import kotlin.math.abs
import kotlin.math.roundToInt

class PersonDetectionSmoother(
    private val smoothingFactor: Float = 0.65f,
    private val predictionFactor: Float = 0.35f,
    private val maxPredictionPixels: Int = 120,
    private val extraPaddingX: Float = 0.12f,
    private val extraPaddingY: Float = 0.08f
) {

    private var currentBox: PersonBoundingBox? = null

    private var previousRawBox: PersonBoundingBox? = null

    private var velocityX = 0f
    private var velocityY = 0f
    private var velocityWidth = 0f
    private var velocityHeight = 0f

    fun update(
        detectedBox: PersonBoundingBox?,
        frameWidth: Int,
        frameHeight: Int
    ): PersonBoundingBox? {

        if (detectedBox == null) {
            return predict(
                frameWidth = frameWidth,
                frameHeight = frameHeight
            )
        }

        val previous = previousRawBox

        if (previous != null) {

            velocityX =
                (detectedBox.x - previous.x).toFloat()

            velocityY =
                (detectedBox.y - previous.y).toFloat()

            velocityWidth =
                (detectedBox.width - previous.width).toFloat()

            velocityHeight =
                (detectedBox.height - previous.height).toFloat()
        }

        previousRawBox = detectedBox

        val predictedX =
            detectedBox.x +
                    (
                            velocityX *
                                    predictionFactor
                            ).coerceIn(
                            -maxPredictionPixels.toFloat(),
                            maxPredictionPixels.toFloat()
                        )

        val predictedY =
            detectedBox.y +
                    (
                            velocityY *
                                    predictionFactor
                            ).coerceIn(
                            -maxPredictionPixels.toFloat(),
                            maxPredictionPixels.toFloat()
                        )

        val predictedWidth =
            detectedBox.width +
                    velocityWidth *
                    predictionFactor

        val predictedHeight =
            detectedBox.height +
                    velocityHeight *
                    predictionFactor

        val targetBox =
            addPadding(
                x = predictedX.roundToInt(),
                y = predictedY.roundToInt(),
                width = predictedWidth.roundToInt(),
                height = predictedHeight.roundToInt(),
                frameWidth = frameWidth,
                frameHeight = frameHeight
            )

        val previousSmooth =
            currentBox

        if (previousSmooth == null) {

            currentBox = targetBox

            return targetBox
        }

        val smoothX =
            lerp(
                previousSmooth.x,
                targetBox.x,
                smoothingFactor
            )

        val smoothY =
            lerp(
                previousSmooth.y,
                targetBox.y,
                smoothingFactor
            )

        val smoothWidth =
            lerp(
                previousSmooth.width,
                targetBox.width,
                smoothingFactor
            )

        val smoothHeight =
            lerp(
                previousSmooth.height,
                targetBox.height,
                smoothingFactor
            )

        val result =
            clampBox(
                PersonBoundingBox(
                    x = smoothX,
                    y = smoothY,
                    width = smoothWidth,
                    height = smoothHeight
                ),
                frameWidth,
                frameHeight
            )

        currentBox = result

        return result
    }

    private fun predict(
        frameWidth: Int,
        frameHeight: Int
    ): PersonBoundingBox? {

        val box =
            currentBox
                ?: return null

        val predictedX =
            (
                    box.x +
                            velocityX *
                            predictionFactor
                    ).roundToInt()

        val predictedY =
            (
                    box.y +
                            velocityY *
                            predictionFactor
                    ).roundToInt()

        val predictedWidth =
            (
                    box.width +
                            velocityWidth *
                            predictionFactor
                    ).roundToInt()

        val predictedHeight =
            (
                    box.height +
                            velocityHeight *
                            predictionFactor
                    ).roundToInt()

        val result =
            clampBox(
                PersonBoundingBox(
                    x = predictedX,
                    y = predictedY,
                    width = predictedWidth,
                    height = predictedHeight
                ),
                frameWidth,
                frameHeight
            )

        currentBox = result

        return result
    }

    private fun addPadding(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        frameWidth: Int,
        frameHeight: Int
    ): PersonBoundingBox {

        val paddingX =
            (
                    width *
                            extraPaddingX
                    ).roundToInt()

        val paddingY =
            (
                    height *
                            extraPaddingY
                    ).roundToInt()

        return clampBox(
            PersonBoundingBox(
                x = x - paddingX,
                y = y - paddingY,
                width = width + paddingX * 2,
                height = height + paddingY * 2
            ),
            frameWidth,
            frameHeight
        )
    }

    private fun clampBox(
        box: PersonBoundingBox,
        frameWidth: Int,
        frameHeight: Int
    ): PersonBoundingBox {

        val x =
            box.x.coerceIn(
                0,
                (frameWidth - 1).coerceAtLeast(0)
            )

        val y =
            box.y.coerceIn(
                0,
                (frameHeight - 1).coerceAtLeast(0)
            )

        val width =
            box.width.coerceIn(
                1,
                frameWidth - x
            )

        val height =
            box.height.coerceIn(
                1,
                frameHeight - y
            )

        return PersonBoundingBox(
            x = x,
            y = y,
            width = width,
            height = height
        )
    }

    private fun lerp(
        from: Int,
        to: Int,
        factor: Float
    ): Int {

        return (
                from +
                        (to - from) *
                        factor
                ).roundToInt()
    }

    fun reset() {

        currentBox = null

        previousRawBox = null

        velocityX = 0f
        velocityY = 0f
        velocityWidth = 0f
        velocityHeight = 0f
    }
}