package com.example.deleteme.visions

data class DeleteMeDebugState(
    val handState: HandState =
        HandState.NOT_DETECTED,

    val personDetected: Boolean =
        false,

    val personBoundingBox:
    PersonBoundingBox? =
        null
)