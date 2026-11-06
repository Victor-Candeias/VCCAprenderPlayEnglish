package com.vcc.aprender.playenglish

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vcc.aprender.playenglish.plugin.PlayEnglishPlugin
import com.vcc.aprender.playenglish.plugin.PluginSessionStateHolder
import com.vcc.aprender.pluginsdk.GameProtocol
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Device/emulator tests for the end-to-end holder and GameProtocol v1. */
@RunWith(AndroidJUnit4::class)
class Pe020HostSessionInstrumentedTest {
    @Test fun verifiedResultIsEmittedOnceWithRealCorrectAndTotalCounts() {
        val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 42L)
        val holder = PluginSessionStateHolder.start(request)
        assertTrue(holder.beginQuiz(listOf("q1", "q2", "q3")))
        assertTrue(holder.recordAnswer("q1", true))
        assertFalse(holder.recordAnswer("q1", false))
        assertTrue(holder.recordAnswer("q2", false))
        assertNull(holder.finishQuizOnce(1, 3))
        assertTrue(holder.recordAnswer("q3", true))
        assertNull(holder.finishQuizOnce(1, 3)) // Wrong UI score must not finalize.
        assertNull(holder.finishQuizOnce(2, 2)) // Wrong total must not finalize.
        val result = requireNotNull(holder.finishQuizOnce(2, 3))
        val parsed = requireNotNull(GameProtocol.readResult(result, request))
        assertEquals(2, parsed.correctAnswers)
        assertEquals(3, parsed.totalQuestions)
        assertEquals(request, parsed.request)
        assertNull(holder.finishQuizOnce(2, 3))
        assertFalse(holder.cancel())
        assertFalse(holder.recordAnswer("q3", false))
    }

    @Test fun canceledOrIncompleteSessionCannotProduceResult() {
        val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 43L)
        val holder = PluginSessionStateHolder.start(request)
        assertFalse(holder.beginQuiz(emptyList()))
        assertTrue(holder.beginQuiz(listOf("q1", "q2")))
        assertTrue(holder.recordAnswer("q1", false))
        assertNull(holder.finishQuizOnce(0, 2))
        assertTrue(holder.cancel())
        assertNull(holder.finishQuizOnce(0, 2))
    }

    @Test fun zeroCorrectIsValidButOnlyAfterAnsweringEveryQuestion() {
        val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 44L)
        val holder = PluginSessionStateHolder.start(request)
        assertTrue(holder.beginQuiz(listOf("q1", "q2")))
        assertTrue(holder.recordAnswer("q1", false))
        assertTrue(holder.recordAnswer("q2", false))
        val result = requireNotNull(holder.finishQuizOnce(0, 2))
        assertEquals(0, GameProtocol.readResult(result, request)?.correctAnswers)
        assertEquals(2, GameProtocol.readResult(result, request)?.totalQuestions)
    }
}
