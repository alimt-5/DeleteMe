package com.example.deleteme.visions

data class HandDetectionState(
    val handState: HandState = HandState.NOT_DETECTED,
    val extendedFingerCount: Int = 0
)