package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.plugin.PlayEnglishPlugin
import com.vcc.aprender.playenglish.plugin.PluginSessionSnapshot
import com.vcc.aprender.playenglish.plugin.PluginSessionStateHolder
import com.vcc.aprender.playenglish.plugin.QuizProgress
import com.vcc.aprender.playenglish.plugin.QuizProgressSnapshot
import com.vcc.aprender.playenglish.plugin.SessionStatus
import com.vcc.aprender.pluginsdk.GameProtocol
import org.junit.Assert.*
import org.junit.Test

class PluginSessionLifecycleTest {
    private val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 45L)

    @Test fun answeredQuestionsSurviveRotationWithoutDuplicates() {
        val old = PluginSessionStateHolder.start(request)
        assertTrue(old.beginQuiz(listOf("q1", "q2", "q3")))
        assertTrue(old.recordAnswer("q1", true))
        assertTrue(old.recordAnswer("q2", false))
        val restored = requireNotNull(PluginSessionStateHolder.restore(request, old.snapshot()))
        assertEquals(2, restored.answeredCount)
        assertTrue(restored.beginQuiz(listOf("q1", "q2", "q3")))
        assertEquals(2, restored.answeredCount)
        assertFalse(restored.recordAnswer("q1", false))
        assertFalse(restored.beginQuiz(listOf("other")))
        assertTrue(restored.recordAnswer("q3", true))
        assertEquals(3, restored.answeredCount)
        assertEquals(
            mapOf("q1" to true, "q2" to false, "q3" to true),
            restored.snapshot().quiz?.answers
        )
    }

    @Test fun canceledSessionIsTerminalAfterProcessRecreation() {
        val current = PluginSessionStateHolder.start(request)
        assertTrue(current.beginQuiz(listOf("first", "second")))
        assertTrue(current.recordAnswer("first", true))
        assertTrue(current.cancel())
        assertFalse(current.cancel())
        assertFalse(current.beginQuiz(listOf("new")))
        assertFalse(current.recordAnswer("second", true))
        val restored = requireNotNull(PluginSessionStateHolder.restore(request, current.snapshot()))
        assertTrue(restored.isCanceled)
        assertFalse(restored.isActive)
        assertFalse(restored.recordAnswer("second", true))
    }

    @Test fun progressRejectsTamperingWithOriginalSessionProfileAndGame() {
        val current = PluginSessionStateHolder.start(request)
        current.beginQuiz(listOf("first"))
        current.recordAnswer("first", true)
        val saved = current.snapshot()
        assertNull(PluginSessionStateHolder.restore(request.copy(profileId = 46L), saved))
        assertNull(PluginSessionStateHolder.restore(
            request.copy(sessionId = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 45L).sessionId),
            saved
        ))
        assertNull(PluginSessionStateHolder.restore(request, saved.copy(gameId = "com.other.game")))
    }

    @Test fun rejectsCompletedOrMalformedSnapshotsClaimingActiveStatus() {
        val done = QuizProgress(listOf("q1"))
        done.recordAnswer("q1", true)
        assertNotNull(done.finishIfComplete())
        val completed = PluginSessionSnapshot(
            request.gameId, request.sessionId, request.profileId,
            SessionStatus.ACTIVE, done.snapshot()
        )
        assertNull(PluginSessionStateHolder.restore(request, completed))
        assertNull(PluginSessionStateHolder.restore(
            request, completed.copy(status = SessionStatus.RESULT_SENT, quiz = null)
        ))
        assertNotNull(PluginSessionStateHolder.restore(
            request, completed.copy(status = SessionStatus.RESULT_SENT)
        ))
        assertNull(PluginSessionStateHolder.restore(
            request, completed.copy(quiz = QuizProgressSnapshot(
                listOf("q1"), mapOf("q2" to true), false
            ))
        ))
    }

    @Test fun rejectsGiantSnapshotsAndDuplicateOrBlankQuestions() {
        val session = PluginSessionStateHolder.start(request)
        assertFalse(session.beginQuiz(emptyList()))
        assertFalse(session.beginQuiz(listOf("q", "q")))
        assertFalse(session.beginQuiz(listOf(" ")))
        assertFalse(session.beginQuiz(listOf("x".repeat(81))))
        assertFalse(session.beginQuiz((0..500).map { "q$it" }))
        assertTrue(session.beginQuiz((0 until 500).map { "q$it" }))
        assertFalse(session.beginQuiz(listOf("changed")))
    }

    @Test fun snapshotCopiesDoNotShareMutableAnswerState() {
        val quiz = QuizProgress(listOf("q1", "q2"))
        quiz.recordAnswer("q1", true)
        val snapshot = quiz.snapshot()
        quiz.recordAnswer("q2", false)
        assertEquals(1, snapshot.answers.size)
        val restored = requireNotNull(QuizProgress.restore(snapshot))
        assertEquals(1, restored.answeredCount)
        assertTrue(restored.recordAnswer("q2", false))
    }
}
