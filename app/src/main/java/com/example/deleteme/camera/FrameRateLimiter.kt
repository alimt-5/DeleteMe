package com.example.deleteme.camera

class FrameRateLimiter(targetFps: Int) {

    private val intervalNs = 1_000_000_000L / targetFps
    private var lastProcessTimeNs = 0L

    fun shouldProcess(): Boolean {
        val now = System.nanoTime()

        if (now - lastProcessTimeNs < intervalNs) {
            return false
        }
        lastProcessTimeNs = now
        return true
    }

    fun reset() {
        lastProcessTimeNs = 0L
    }
}