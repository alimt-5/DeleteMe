package com.example.deleteme.visions

data class HandDetectionResult(
    val state: HandState,
    val extendedFingerCount: Int,
    val landmarks: List<HandLandmarkPoint> = emptyList()
)