package com.example.deleteme.camera

import android.graphics.Bitmap

data class MLFrame(
    val bitmap: Bitmap,
    val scaleX: Float,
    val scaleY: Float
)