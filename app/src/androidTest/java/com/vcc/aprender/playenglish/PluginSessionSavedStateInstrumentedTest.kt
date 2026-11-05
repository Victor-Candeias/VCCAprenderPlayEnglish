package com.vcc.aprender.playenglish

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vcc.aprender.playenglish.plugin.PlayEnglishPlugin
import com.vcc.aprender.playenglish.plugin.PluginSessionSavedState
import com.vcc.aprender.playenglish.plugin.PluginSessionStateHolder
import com.vcc.aprender.playenglish.plugin.SessionStatus
import com.vcc.aprender.pluginsdk.GameProtocol
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PluginSessionSavedStateInstrumentedTest {
    private val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 99L)

    @Test fun bundleRoundTripRestoresSameQuizAndAnswers() {
        val current = PluginSessionStateHolder.start(request)
        assertTrue(current.beginQuiz(listOf("cat", "red", "three")))
        assertTrue(current.recordAnswer("cat", true))
        assertTrue(current.recordAnswer("red", false))
        val outState = Bundle()
        PluginSessionSavedState.write(outState, current.snapshot())
        val decoded = requireNotNull(PluginSessionSavedState.read(outState, request))
        val restored = requireNotNull(PluginSessionStateHolder.restore(request, decoded))
        assertEquals(2, restored.answeredCount)
        assertFalse(restored.recordAnswer("cat", false))
        assertTrue(restored.recordAnswer("three", true))
        val result = requireNotNull(restored.finishQuizOnce())
        assertEquals(2, GameProtocol.readResult(result, request)?.correctAnswers)
        assertEquals(3, GameProtocol.readResult(result, request)?.totalQuestions)
        assertNull(restored.finishQuizOnce())
        assertEquals(SessionStatus.RESULT_SENT, restored.snapshot().status)
    }

    @Test fun finishedResultStateCannotSendTwiceAfterRecreation() {
        val holder = PluginSessionStateHolder.start(request)
        holder.beginQuiz(listOf("q1"))
        holder.recordAnswer("q1", true)
        assertNotNull(holder.finishQuizOnce())
        val out = Bundle()
        PluginSessionSavedState.write(out, holder.snapshot())
        val decoded = requireNotNull(PluginSessionSavedState.read(out, request))
        val restored = requireNotNull(PluginSessionStateHolder.restore(request, decoded))
        assertTrue(restored.wasResultSent)
        assertFalse(restored.isActive)
        assertNull(restored.finishQuizOnce())
        assertFalse(restored.cancel())
    }

    @Test fun canceledSessionCannotGenerateScoresAfterRestore() {
        val holder = PluginSessionStateHolder.start(request)
        holder.beginQuiz(listOf("q1"))
        holder.recordAnswer("q1", true)
        assertTrue(holder.cancel())
        val out = Bundle()
        PluginSessionSavedState.write(out, holder.snapshot())
        val restored = requireNotNull(PluginSessionStateHolder.restore(
            request, requireNotNull(PluginSessionSavedState.read(out, request))
        ))
        assertTrue(restored.isCanceled)
        assertNull(restored.finishQuizOnce())
    }

    @Test fun corruptedStateOrChangedHostSessionFailsClosed() {
        val holder = PluginSessionStateHolder.start(request)
        holder.beginQuiz(listOf("q1"))
        val out = Bundle()
        PluginSessionSavedState.write(out, holder.snapshot())
        assertNull(PluginSessionSavedState.read(out, request.copy(profileId = 100L)))
        val newSession = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 99L)
        assertNull(PluginSessionSavedState.read(out, newSession))
        assertNull(PluginSessionSavedState.read(Bundle(), request))
    }
}
