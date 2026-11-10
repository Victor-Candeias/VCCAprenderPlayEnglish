package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.domain.model.*
import com.vcc.aprender.playenglish.domain.repository.QuizContentRepository
import com.vcc.aprender.playenglish.domain.usecase.*
import com.vcc.aprender.playenglish.domain.game.QuizProgress
import com.vcc.aprender.playenglish.domain.game.QuizProgressSnapshot
import com.vcc.aprender.playenglish.plugin.PlayEnglishPlugin
import com.vcc.aprender.playenglish.plugin.PluginSessionStateHolder
import com.vcc.aprender.pluginsdk.GameProtocol
import com.vcc.aprender.pluginsdk.GameValidation
import org.junit.Assert.*
import org.junit.Test

/** PE-040 deterministic boundary tests for deck, score and session. */
class Pe040CatalogDeckScoringRegressionTest {
    private fun question(i: Int) = QuizQuestion("q.$i", "Pergunta $i",
        listOf(QuizChoice("a.$i", "Apple $i"), QuizChoice("b.$i", "Banana $i"),
            QuizChoice("c.$i", "Cherry $i")), "b.$i")

    private fun load(n: Int) = LoadQuizUseCase(object : QuizContentRepository {
        override fun loadQuestions(): List<QuizQuestion> = (1..n).map(::question)
    }).execute()

    @Test fun noQuestionsCannotProduceArtificialScore() {
        val empty = load(0)
        assertEquals(QuizPhase.NOT_STARTED, empty.phase)
        assertEquals(empty, CompleteQuizUseCase().execute(empty))
        assertNull(empty.score)
    }

    @Test fun zeroAndAllCorrectScoresPreserveSingleAttempt() {
        for (correct in listOf(false, true)) {
            var state = load(8)
            val answer = AnswerQuestionUseCase()
            for (q in state.questions) {
                val choice = if (correct) q.correctChoiceId else q.choices.first { it.id != q.correctChoiceId }.id
                val next = answer.execute(state, q.id, choice)
                assertEquals(next, answer.execute(next, q.id, q.correctChoiceId))
                assertEquals(state.answeredCount + 1, next.answeredCount)
                state = next
            }
            val complete = CompleteQuizUseCase().execute(state)
            assertEquals(if (correct) 8 else 0, complete.score!!.correctAnswers)
            assertEquals(8, complete.score!!.totalQuestions)
            assertEquals(complete, CompleteQuizUseCase().execute(complete))
            assertEquals(complete, answer.execute(complete, "q.1", "a.1"))
        }
    }

    @Test fun invalidAnswerAndCancelNeverProduceScore() {
        val initial = load(2)
        val answer = AnswerQuestionUseCase()
        assertEquals(initial, answer.execute(initial, "missing", "a.1"))
        assertEquals(initial, answer.execute(initial, "q.1", "missing"))
        val partial = answer.execute(initial, "q.1", "b.1")
        assertEquals(partial, CompleteQuizUseCase().execute(partial))
        val canceled = CancelQuizUseCase().execute(partial)
        assertEquals(QuizPhase.CANCELED, canceled.phase)
        assertNull(canceled.score)
        assertEquals(canceled, answer.execute(canceled, "q.2", "b.2"))
    }

    @Test fun everySeedPreservesUniqueDeckAndBalancedABCSlots() {
        val shuffle = ShuffleQuizDeckUseCase()
        for (n in listOf(1, 2, 3, 9, 100)) {
            val source = (1..n).map(::question)
            val shuffled = shuffle.execute(source, 2026040L)
            assertEquals(shuffled, shuffle.execute(source, 2026040L))
            assertEquals(n, shuffled.map { it.id }.distinct().size)
            assertEquals(source.map { it.id }.toSet(), shuffled.map { it.id }.toSet())
            shuffled.forEach { q ->
                assertEquals(3, q.choices.map { it.id }.distinct().size)
                assertEquals(1, q.choices.count { it.id == q.correctChoiceId })
            }
            val counts = (0..2).map { slot ->
                shuffled.count { it.choices[slot].id == it.correctChoiceId }
            }
            assertTrue(counts.maxOrNull()!! - counts.minOrNull()!! <= 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            shuffle.execute(listOf(question(1), question(1)), 1L)
        }
        val duplicate = question(1).copy(choices = listOf(
            QuizChoice("a.1", "Dog"), QuizChoice("b.1", " dog "),
            QuizChoice("c.1", "Cat")))
        assertThrows(IllegalArgumentException::class.java) {
            shuffle.execute(listOf(duplicate), 1L)
        }
    }

    @Test fun snapshotsRejectTamperingAndRestoreExactlyOnce() {
        assertNull(QuizProgress.restore(QuizProgressSnapshot(
            listOf("a", "b"), mapOf("foreign" to true), false)))
        assertNull(QuizProgress.restore(QuizProgressSnapshot(
            listOf("a", "b"), mapOf("a" to true), true)))
        assertNull(QuizProgress.restore(QuizProgressSnapshot(
            listOf("a", "a"), emptyMap(), false)))
        val original = QuizProgress(listOf("a", "b"))
        assertTrue(original.recordAnswer("a", true))
        val snapshot = original.snapshot()
        assertTrue(original.recordAnswer("b", false))
        val restored = requireNotNull(QuizProgress.restore(snapshot))
        assertEquals(1, restored.answeredCount)
        assertFalse(restored.recordAnswer("a", false))
        assertTrue(restored.recordAnswer("b", false))
        assertEquals(1, requireNotNull(restored.finishIfComplete()).correctAnswers)
        assertNull(restored.finishIfComplete())
    }

    @Test fun protocolRejectsInvalidLimitsVersionsAndScores() {
        val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 42L)
        fun valid(correct: Int, total: Int, version: Int = 1) =
            GameValidation.validResult(request, request.gameId, request.sessionId,
                request.profileId, version, correct, total)
        assertTrue(valid(0, 1))
        assertTrue(valid(10_000, 10_000))
        for ((correct, total) in listOf(-1 to 1, 2 to 1, 0 to 0, 0 to 10_001)) {
            assertFalse(valid(correct, total))
        }
        assertFalse(valid(1, 2, 2))
        assertFalse(GameValidation.validResult(request, request.gameId, "wrong-session",
            request.profileId, 1, 1, 2))
    }

    @Test fun hostRejectsConflictingScoreAndSendsOnlyOnce() {
        val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 42L)
        val session = PluginSessionStateHolder.start(request)
        assertTrue(session.beginQuiz(listOf("q1", "q2")))
        assertTrue(session.recordAnswer("q1", true))
        assertNull(session.finishQuizOnce(1, 2))
        assertTrue(session.recordAnswer("q2", false))
        assertNull(session.finishQuizOnce(0, 2))
        assertNull(session.finishQuizOnce(1, 3))
        assertFalse(session.recordAnswer("q2", true))
        assertNotNull(session.finishQuizOnce(1, 2))
        assertNull(session.finishQuizOnce(1, 2))
        assertFalse(session.isActive)
    }
}
