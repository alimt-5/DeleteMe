package com.example.deleteme.visions

data class DeleteMeState(
    val handClosed: Boolean = false,
    val handDetected: Boolean = false,
    val personDetected: Boolean = false,
    val personBoundingBox: PersonBoundingBox? = null
)