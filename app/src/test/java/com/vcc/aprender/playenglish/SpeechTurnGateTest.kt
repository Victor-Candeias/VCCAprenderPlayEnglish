package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.audio.SpeechTurnGate
import org.junit.Assert.*
import org.junit.Test

class SpeechTurnGateTest {
    @Test fun refusesOverlappingRequestsAndOnlyUnlocksOnActiveCompletion() {
        val gate = SpeechTurnGate()
        assertTrue(gate.tryBegin("one"))
        assertTrue(gate.isSpeaking)
        repeat(20) { assertFalse(gate.tryBegin("second-$it")) }
        assertFalse(gate.finish("stale"))
        assertTrue(gate.isSpeaking)
        assertTrue(gate.finish("one"))
        assertFalse(gate.isSpeaking)
        assertTrue(gate.tryBegin("two"))
    }

    @Test fun oldCallbacksAfterStopCannotReleaseNewUtterance() {
        val gate = SpeechTurnGate()
        assertTrue(gate.tryBegin("old"))
        assertTrue(gate.cancel())
        assertTrue(gate.tryBegin("new"))
        assertFalse(gate.finish("old"))
        assertTrue(gate.isSpeaking)
        assertTrue(gate.finish("new"))
        assertFalse(gate.isSpeaking)
    }

    @Test fun stopErrorAndDoneAreTerminalOncePerId() {
        val gate = SpeechTurnGate()
        assertTrue(gate.tryBegin("failed"))
        assertTrue(gate.finish("failed"))
        assertFalse(gate.finish("failed"))
        assertTrue(gate.tryBegin("stopped"))
        assertTrue(gate.cancel())
        assertFalse(gate.cancel())
        assertFalse(gate.finish("stopped"))
        assertTrue(gate.tryBegin("done"))
        assertTrue(gate.finish("done"))
    }

    @Test fun invalidIdsAndLateCallbacksCannotStartOrRelease() {
        val gate = SpeechTurnGate()
        assertFalse(gate.tryBegin(""))
        assertFalse(gate.tryBegin("  "))
        assertFalse(gate.finish(null))
        assertFalse(gate.finish("unknown"))
        assertTrue(gate.tryBegin("valid"))
        assertFalse(gate.finish(null))
        assertTrue(gate.isSpeaking)
    }
}
