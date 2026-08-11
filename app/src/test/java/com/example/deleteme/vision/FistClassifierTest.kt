package com.example.deleteme.vision

import org.junit.Assert.assertEquals
import org.junit.Test

class FistClassifierTest {

    private val classifier = FistClassifier()

    /**
     * Builds a 21-point MediaPipe-style landmark list. Every point defaults
     * to the wrist position; callers override only the tip/pip pairs that
     * matter for the scenario being tested.
     */
    private fun landmarks(overrides: Map<Int, HandLandmarkPoint>): List<HandLandmarkPoint> {
        val wrist = HandLandmarkPoint(x = 0f, y = 0f, z = 0f)
        return (0..20).map { index -> overrides[index] ?: wrist }
    }

    private fun atDistance(distanceFromWrist: Float) =
        HandLandmarkPoint(x = 0f, y = distanceFromWrist, z = 0f)

    @Test
    fun `fewer than 21 landmarks is NOT_DETECTED`() {
        val incomplete = landmarks(emptyMap()).dropLast(1)

        val result = classifier.classify(incomplete)

        assertEquals(HandState.NOT_DETECTED, result.state)
        assertEquals(0, result.extendedFingerCount)
    }

    @Test
    fun `all four fingers extended is OPEN`() {
        val points = landmarks(
            mapOf(
                6 to atDistance(0.10f), 8 to atDistance(0.20f),   // index: tip farther than pip
                10 to atDistance(0.10f), 12 to atDistance(0.20f), // middle
                14 to atDistance(0.10f), 16 to atDistance(0.20f), // ring
                18 to atDistance(0.10f), 20 to atDistance(0.20f)  // pinky
            )
        )

        val result = classifier.classify(points)

        assertEquals(HandState.OPEN, result.state)
        assertEquals(4, result.extendedFingerCount)
    }

    @Test
    fun `all four fingers curled is CLOSED`() {
        val points = landmarks(
            mapOf(
                6 to atDistance(0.10f), 8 to atDistance(0.05f),   // index: tip closer than pip
                10 to atDistance(0.10f), 12 to atDistance(0.05f), // middle
                14 to atDistance(0.10f), 16 to atDistance(0.05f), // ring
                18 to atDistance(0.10f), 20 to atDistance(0.05f)  // pinky
            )
        )

        val result = classifier.classify(points)

        assertEquals(HandState.CLOSED, result.state)
        assertEquals(0, result.extendedFingerCount)
    }

    @Test
    fun `exactly two fingers extended is still CLOSED`() {
        val points = landmarks(
            mapOf(
                6 to atDistance(0.10f), 8 to atDistance(0.20f),  // extended
                10 to atDistance(0.10f), 12 to atDistance(0.20f), // extended
                14 to atDistance(0.10f), 16 to atDistance(0.05f), // curled
                18 to atDistance(0.10f), 20 to atDistance(0.05f)  // curled
            )
        )

        val result = classifier.classify(points)

        // The OPEN threshold is >= 3 extended fingers, so 2 stays CLOSED.
        assertEquals(HandState.CLOSED, result.state)
        assertEquals(2, result.extendedFingerCount)
    }

    @Test
    fun `exactly three fingers extended is OPEN`() {
        val points = landmarks(
            mapOf(
                6 to atDistance(0.10f), 8 to atDistance(0.20f),  // extended
                10 to atDistance(0.10f), 12 to atDistance(0.20f), // extended
                14 to atDistance(0.10f), 16 to atDistance(0.20f), // extended
                18 to atDistance(0.10f), 20 to atDistance(0.05f)  // curled
            )
        )

        val result = classifier.classify(points)

        assertEquals(HandState.OPEN, result.state)
        assertEquals(3, result.extendedFingerCount)
    }
}
