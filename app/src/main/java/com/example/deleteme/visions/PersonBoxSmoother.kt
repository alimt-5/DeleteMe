package com.example.deleteme.visions

import kotlin.math.abs
import kotlin.math.roundToInt

class PersonBoxSmoother(
    private val smoothingFactor: Float = 0.45f
) {

    private var currentBox:
            PersonBoundingBox? = null

    fun update(
        newBox: PersonBoundingBox
    ): PersonBoundingBox {

        val previous =
            currentBox

        if (previous == null) {

            currentBox =
                newBox

            return newBox
        }

        /*
         * Ignore extremely large jumps.
         *
         * This protects the replacement area from
         * sudden detector errors.
         */
        val maxPositionJump =
            0.30f

        val maxSizeChange =
            0.40f

        val positionJumpX =
            abs(
                newBox.x -
                        previous.x
            ).toFloat() /
                    previous.width
                        .coerceAtLeast(1)

        val positionJumpY =
            abs(
                newBox.y -
                        previous.y
            ).toFloat() /
                    previous.height
                        .coerceAtLeast(1)

        val widthChange =
            abs(
                newBox.width -
                        previous.width
            ).toFloat() /
                    previous.width
                        .coerceAtLeast(1)

        val heightChange =
            abs(
                newBox.height -
                        previous.height
            ).toFloat() /
                    previous.height
                        .coerceAtLeast(1)

        /*
         * If the detector suddenly jumps too far,
         * keep the previous box for this frame.
         */
        if (
            positionJumpX > maxPositionJump ||
            positionJumpY > maxPositionJump ||
            widthChange > maxSizeChange ||
            heightChange > maxSizeChange
        ) {

            return previous
        }

        val factor =
            smoothingFactor.coerceIn(
                0.0f,
                1.0f
            )

        val x =
            smooth(
                previous.x,
                newBox.x,
                factor
            )

        val y =
            smooth(
                previous.y,
                newBox.y,
                factor
            )

        val width =
            smooth(
                previous.width,
                newBox.width,
                factor
            )

        val height =
            smooth(
                previous.height,
                newBox.height,
                factor
            )

        val smoothed =
            PersonBoundingBox(
                x = x,
                y = y,
                width =
                width.coerceAtLeast(1),
                height =
                height.coerceAtLeast(1)
            )

        currentBox =
            smoothed

        return smoothed
    }

    private fun smooth(
        oldValue: Int,
        newValue: Int,
        factor: Float
    ): Int {

        return (
                oldValue +
                        (
                                newValue -
                                        oldValue
                                ) * factor
                ).roundToInt()
    }

    fun current():
            PersonBoundingBox? {

        return currentBox
    }

    fun reset() {

        currentBox =
            null
    }
}