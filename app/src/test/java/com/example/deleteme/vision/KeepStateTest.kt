package com.example.deleteme.vision

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeepStateTest {

    @Test
    fun `starts at the given initial state`() {
        val keepState = KeepState(keepForSeconds = 1f, fps = 10, initialState = false)

        assertFalse(keepState.currentState())
    }

    @Test
    fun `matching updates never change the state`() {
        val keepState = KeepState(keepForSeconds = 1f, fps = 10, initialState = false)

        repeat(50) {
            assertFalse(keepState.update(newState = false))
        }
    }

    @Test
    fun `a single noisy opposite reading does not flip the state`() {
        val keepState = KeepState(keepForSeconds = 1f, fps = 10, initialState = false)
        // Build up full confidence in "false" first (keepFor = 1s * 10fps = 10 frames).
        repeat(10) { keepState.update(newState = false) }

        val result = keepState.update(newState = true)

        assertFalse(result)
    }

    @Test
    fun `sustained opposite readings eventually flip the state`() {
        val keepState = KeepState(keepForSeconds = 1f, fps = 10, initialState = false)
        repeat(10) { keepState.update(newState = false) }

        var result = false
        // A fully-confirmed state needs keepFor + 1 consecutive opposing
        // readings to flip (10 to unwind the counter, 1 more to cross 0).
        repeat(11) {
            result = keepState.update(newState = true)
        }

        assertTrue(result)
        assertTrue(keepState.currentState())
    }

    @Test
    fun `reset returns to the given initial state`() {
        val keepState = KeepState(keepForSeconds = 1f, fps = 10, initialState = false)
        repeat(11) { keepState.update(newState = true) }
        assertTrue(keepState.currentState())

        keepState.reset(initialState = false)

        assertFalse(keepState.currentState())
    }
}
