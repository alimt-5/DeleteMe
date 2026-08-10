package com.example.deleteme.visions

import android.graphics.Bitmap
import kotlin.math.roundToInt

class MlFrameResizer(
    private val targetWidth: Int = 640,
    private val targetHeight: Int = 360
) {

    fun resize(
        source: Bitmap
    ): Bitmap {

        if (
            source.width == targetWidth &&
            source.height == targetHeight
        ) {
            return source
        }

        return Bitmap.createScaledBitmap(
            source,
            targetWidth,
            targetHeight,
            true
        )
    }

    fun scaleX(
        originalWidth: Int,
        resizedWidth: Int
    ): Float {

        if (resizedWidth == 0) {
            return 1f
        }

        return originalWidth.toFloat() /
                resizedWidth.toFloat()
    }

    fun scaleY(
        originalHeight: Int,
        resizedHeight: Int
    ): Float {

        if (resizedHeight == 0) {
            return 1f
        }

        return originalHeight.toFloat() /
                resizedHeight.toFloat()
    }

    fun mapX(
        x: Int,
        originalWidth: Int,
        resizedWidth: Int
    ): Int {

        return (
                x.toFloat() *
                        scaleX(
                            originalWidth,
                            resizedWidth
                        )
                ).roundToInt()
    }

    fun mapY(
        y: Int,
        originalHeight: Int,
        resizedHeight: Int
    ): Int {

        return (
                y.toFloat() *
                        scaleY(
                            originalHeight,
                            resizedHeight
                        )
                ).roundToInt()
    }
}