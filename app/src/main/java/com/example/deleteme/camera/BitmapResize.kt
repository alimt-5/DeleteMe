package com.example.deleteme.camera

import android.graphics.Bitmap

fun Bitmap.resizeForML(maxWidth: Int = 640, maxHeight: Int = 360): Bitmap {

    val widthRatio = maxWidth.toFloat() / width
    val heightRatio = maxHeight.toFloat() / height
    val scale = minOf(widthRatio, heightRatio)
    if (scale >= 1f) return this
    val newWidth = (width * scale).toInt()
    val newHeight = (height * scale).toInt()
    return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
}