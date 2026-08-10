package com.example.deleteme.vision

class KeepState(
    keepForSeconds: Float,
    fps: Int = 30,
    initialState: Boolean = false
) {

    private var counter = 0

    private val keepFor =
        (keepForSeconds * fps).toInt()

    private var state =
        initialState

    fun update(newState: Boolean): Boolean {

        if (newState == state) {

            counter++

            if (counter > keepFor) {
                counter = keepFor
            }

            return state
        }

        counter--

        if (counter < 0) {

            counter = keepFor

            state = newState
        }

        return state
    }

    fun reset(
        initialState: Boolean = false
    ) {

        counter = 0
        state = initialState
    }

    fun currentState(): Boolean {
        return state
    }
}