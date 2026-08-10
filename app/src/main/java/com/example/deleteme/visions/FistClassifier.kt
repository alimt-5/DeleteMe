package com.example.deleteme.visions

import kotlin.math.sqrt

class FistClassifier {

    private val fingerPairs = listOf(
        8 to 6,
        12 to 10,
        16 to 14,
        20 to 18
    )

    data class Classification(
        val state: HandState,
        val extendedFingerCount: Int
    )

    fun classify(
        landmarks: List<HandLandmarkPoint>
    ): Classification {

        if (landmarks.size < 21) {

            return Classification(
                state = HandState.NOT_DETECTED,
                extendedFingerCount = 0
            )
        }

        val wrist =
            landmarks[0]

        var extendedFingers = 0

        for ((tipIndex, pipIndex) in fingerPairs) {

            val tip =
                landmarks[tipIndex]

            val pip =
                landmarks[pipIndex]

            val tipDistance =
                distance(
                    tip,
                    wrist
                )

            val pipDistance =
                distance(
                    pip,
                    wrist
                )

            if (tipDistance > pipDistance) {

                extendedFingers++
            }
        }

        val state =
            if (extendedFingers >= 3) {

                HandState.OPEN

            } else {

                HandState.CLOSED
            }

        return Classification(
            state = state,
            extendedFingerCount = extendedFingers
        )
    }

    private fun distance(
        pointA: HandLandmarkPoint,
        pointB: HandLandmarkPoint
    ): Float {

        val dx =
            pointA.x - pointB.x

        val dy =
            pointA.y - pointB.y

        return sqrt(
            dx * dx + dy * dy
        )
    }
}