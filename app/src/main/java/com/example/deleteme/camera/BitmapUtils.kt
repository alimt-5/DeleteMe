package com.example.deleteme.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy

fun ImageProxy.toRotatedBitmap(): Bitmap {

    val bitmap = toBitmap()

    val rotation = imageInfo.rotationDegrees

    if (rotation == 0) {
        return bitmap
    }

    val matrix = Matrix()

    matrix.postRotate(rotation.toFloat())

    return Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true
    )
}