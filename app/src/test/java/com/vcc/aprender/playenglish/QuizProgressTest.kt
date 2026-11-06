package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.plugin.QuizProgress
import org.junit.Assert.*
import org.junit.Test

class QuizProgressTest {
    @Test fun exposedInMemoryCountsAreDerivedFromUniqueAnswers() {
        val quiz = QuizProgress(listOf("q1", "q2", "q3"))
        assertEquals(3, quiz.totalQuestions)
        assertEquals(0, quiz.correctCount)
        assertTrue(quiz.recordAnswer("q1", true))
        assertFalse(quiz.recordAnswer("q1", false))
        assertTrue(quiz.recordAnswer("q2", false))
        assertEquals(2, quiz.answeredCount)
        assertEquals(1, quiz.correctCount)
        assertNull(quiz.finishIfComplete())
    }

    @Test fun countsRealAnswersAndFinishesExactlyOnce() {
        val quiz = QuizProgress(listOf("animal.cat", "colour.red", "number.three"))
        assertFalse(quiz.isComplete)
        assertNull(quiz.finishIfComplete())
        assertTrue(quiz.recordAnswer("animal.cat", true))
        assertTrue(quiz.recordAnswer("colour.red", false))
        assertTrue(quiz.recordAnswer("number.three", true))
        assertTrue(quiz.isComplete)
        val result = requireNotNull(quiz.finishIfComplete())
        assertEquals(2, result.correctAnswers)
        assertEquals(3, result.totalQuestions)
        assertNull(quiz.finishIfComplete())
        assertFalse(quiz.recordAnswer("animal.cat", false))
    }

    @Test fun rejectsUnknownOrDuplicateAnswerWithoutChangingScore() {
        val quiz = QuizProgress(listOf("q1", "q2"))
        assertFalse(quiz.recordAnswer("unknown", true))
        assertTrue(quiz.recordAnswer("q1", true))
        assertFalse(quiz.recordAnswer("q1", false))
        assertEquals(1, quiz.answeredCount)
        assertNull(quiz.finishIfComplete())
        assertTrue(quiz.recordAnswer("q2", false))
        assertEquals(1, requireNotNull(quiz.finishIfComplete()).correctAnswers)
    }

    @Test fun supportsGenuineZeroCorrectWithoutZeroQuestions() {
        val quiz = QuizProgress(listOf("q1", "q2"))
        assertTrue(quiz.recordAnswer("q1", false))
        assertTrue(quiz.recordAnswer("q2", false))
        val result = requireNotNull(quiz.finishIfComplete())
        assertEquals(0, result.correctAnswers)
        assertEquals(2, result.totalQuestions)
    }

    @Test fun rejectsEmptyDuplicateOrMoreThanTenThousandQuestions() {
        assertThrows(IllegalArgumentException::class.java) { QuizProgress(emptyList()) }
        assertThrows(IllegalArgumentException::class.java) { QuizProgress(listOf("q1", "q1")) }
        assertThrows(IllegalArgumentException::class.java) { QuizProgress(listOf(" ", "q2")) }
        assertThrows(IllegalArgumentException::class.java) {
            QuizProgress((0..10_000).map { "q$it" })
        }
    }

    @Test fun acceptsProtocolMaximumOfTenThousandQuestions() {
        val quiz = QuizProgress((0 until 10_000).map { "q$it" })
        for (id in 0 until 10_000) {
            assertTrue(quiz.recordAnswer("q$id", id % 2 == 0))
        }
        val score = requireNotNull(quiz.finishIfComplete())
        assertEquals(5_000, score.correctAnswers)
        assertEquals(10_000, score.totalQuestions)
    }
}
