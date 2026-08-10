package com.example.deleteme.vision

data class HandDetectionResult(
    val state: HandState,
    val extendedFingerCount: Int,
    val landmarks: List<HandLandmarkPoint> = emptyList()
)