package com.vcc.aprender.playenglish

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vcc.aprender.playenglish.plugin.PlayEnglishPlugin
import com.vcc.aprender.playenglish.plugin.PluginQuizResultSender
import com.vcc.aprender.playenglish.plugin.QuizProgress
import com.vcc.aprender.pluginsdk.GameContract
import com.vcc.aprender.pluginsdk.GameProtocol
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Tests Android Intent wire format; run on a device/emulator for full validation. */
@RunWith(AndroidJUnit4::class)
class PluginQuizResultSenderInstrumentedTest {
    @Test fun sendsCompletedResultsWithOriginalHostSessionAndNoPersonalData() {
        val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 42L)
        val sender = PluginQuizResultSender(request)
        val quiz = QuizProgress(listOf("q1", "q2", "q3"))

        assertNull(sender.createResultOnce(quiz.finishIfComplete()))
        assertTrue(quiz.recordAnswer("q1", true))
        assertTrue(quiz.recordAnswer("q2", true))
        assertTrue(quiz.recordAnswer("q3", false))

        val intent = requireNotNull(sender.createResultOnce(quiz.finishIfComplete()))
        val parsed = requireNotNull(GameProtocol.readResult(intent, request))
        assertEquals(2, parsed.correctAnswers)
        assertEquals(3, parsed.totalQuestions)
        assertEquals(request, parsed.request)
        assertEquals(
            setOf(
                GameContract.EXTRA_PROTOCOL_VERSION,
                GameContract.EXTRA_GAME_ID,
                GameContract.EXTRA_SESSION_ID,
                GameContract.EXTRA_PROFILE_ID,
                GameContract.EXTRA_CORRECT_ANSWERS,
                GameContract.EXTRA_TOTAL_QUESTIONS
            ),
            requireNotNull(intent.extras).keySet()
        )
        assertNull(sender.createResultOnce(QuizProgress(listOf("second")).apply {
            recordAnswer("second", true)
        }.finishIfComplete()))
    }

    @Test fun cancelOrIncompleteQuizDoesNotGenerateAResult() {
        val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 99L)
        val sender = PluginQuizResultSender(request)
        val incomplete = QuizProgress(listOf("q1", "q2"))
        incomplete.recordAnswer("q1", true)
        assertNull(sender.createResultOnce(incomplete.finishIfComplete()))
        assertNull(sender.createResultOnce(null))
    }

    @Test fun zeroCorrectIsAValidCompletedQuizButZeroTotalIsNotEmitted() {
        val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 10L)
        val sender = PluginQuizResultSender(request)
        val quiz = QuizProgress(listOf("q1"))
        quiz.recordAnswer("q1", false)
        val intent = requireNotNull(sender.createResultOnce(quiz.finishIfComplete()))
        assertEquals(0, GameProtocol.readResult(intent, request)?.correctAnswers)
        assertEquals(1, GameProtocol.readResult(intent, request)?.totalQuestions)
    }

    @Test fun hostRejectsCrossProfileAndCrossSessionReplay() {
        val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 17L)
        val quiz = QuizProgress(listOf("q1"))
        quiz.recordAnswer("q1", true)
        val intent = requireNotNull(PluginQuizResultSender(request).createResultOnce(quiz.finishIfComplete()))
        assertNull(GameProtocol.readResult(intent, request.copy(profileId = 18L)))
        assertNull(GameProtocol.readResult(intent, request.copy(sessionId = GameProtocol.newRequest(
            PlayEnglishPlugin.GAME_ID, 17L
        ).sessionId)))
    }
}
