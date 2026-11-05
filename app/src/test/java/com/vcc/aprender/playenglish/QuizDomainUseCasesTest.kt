package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.domain.model.*
import com.vcc.aprender.playenglish.domain.repository.QuizContentRepository
import com.vcc.aprender.playenglish.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test

class QuizDomainUseCasesTest {
    private val question1 = QuizQuestion(
        "animals.cat", "Gato em inglês?",
        listOf(
            QuizChoice("cat", "Cat"),
            QuizChoice("dog", "Dog"),
            QuizChoice("bird", "Bird")
        ), "cat"
    )
    private val question2 = QuizQuestion(
        "colours.red", "Vermelho em inglês?",
        listOf(
            QuizChoice("blue", "Blue"),
            QuizChoice("red", "Red"),
            QuizChoice("green", "Green")
        ), "red"
    )
    private fun repository(vararg questions: QuizQuestion) =
        object : QuizContentRepository {
            override fun loadQuestions(): List<QuizQuestion> = questions.toList()
        }

    @Test fun emptyOfflineCatalogCannotGenerateArtificialResult() {
        val empty = LoadQuizUseCase(repository()).execute()
        assertEquals(QuizPhase.NOT_STARTED, empty.phase)
        assertNull(empty.score)
        assertEquals(empty, CompleteQuizUseCase().execute(empty))
    }

    @Test fun answeringWrongAndRightChoicesProducesDeterministicRealScore() {
        val original = LoadQuizUseCase(repository(question1, question2)).execute()
        assertEquals(QuizPhase.ACTIVE, original.phase)
        val answer = AnswerQuestionUseCase()
        val first = answer.execute(original, "animals.cat", "cat")
        assertTrue(original.selected.isEmpty())
        assertEquals(1, first.answeredCount)
        assertEquals(first, answer.execute(first, "animals.cat", "dog"))
        assertEquals(first, answer.execute(first, "unknown", "cat"))
        assertEquals(first, answer.execute(first, "colours.red", "not-an-option"))
        assertEquals(first, CompleteQuizUseCase().execute(first))
        val second = answer.execute(first, "colours.red", "green")
        assertTrue(second.allAnswered)
        val completed = CompleteQuizUseCase().execute(second)
        assertEquals(QuizPhase.COMPLETED, completed.phase)
        assertEquals(QuizScore(1, 2), completed.score)
        assertEquals(completed, CompleteQuizUseCase().execute(completed))
        assertEquals(completed, answer.execute(completed, "animals.cat", "bird"))
    }

    @Test fun cancellationIsTerminalAndDoesNotProduceScore() {
        val started = LoadQuizUseCase(repository(question1)).execute()
        val canceled = CancelQuizUseCase().execute(started)
        assertEquals(QuizPhase.CANCELED, canceled.phase)
        assertNull(canceled.score)
        assertEquals(canceled, AnswerQuestionUseCase().execute(canceled, "animals.cat", "cat"))
        assertEquals(canceled, CompleteQuizUseCase().execute(canceled))
        assertEquals(canceled, CancelQuizUseCase().execute(canceled))
    }

    @Test fun invalidContentFailsBeforeStarting() {
        assertThrows(IllegalArgumentException::class.java) {
            QuizQuestion(
                "q", "Prompt",
                listOf(QuizChoice("a", "A"), QuizChoice("a", "Duplicate"), QuizChoice("c", "C")),
                "a"
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            QuizQuestion(
                "q", "Prompt",
                listOf(QuizChoice("a", "A"), QuizChoice("b", "B"), QuizChoice("c", "C")),
                "not-present"
            )
        }
        val duplicateId = repository(question1, question1)
        assertThrows(IllegalArgumentException::class.java) {
            LoadQuizUseCase(duplicateId).execute()
        }
    }

    @Test fun acceptsUnicodeAccentsAndSentences() {
        val sentence = QuizQuestion(
            "sentences.hello", "Como se diz «Bom dia»?",
            listOf(
                QuizChoice("good_morning", "Good morning"),
                QuizChoice("good_night", "Good night"),
                QuizChoice("thank_you", "Thank you")
            ), "good_morning"
        )
        val quiz = LoadQuizUseCase(repository(sentence)).execute()
        val answered = AnswerQuestionUseCase().execute(quiz, sentence.id, "good_morning")
        assertEquals(QuizScore(1, 1), CompleteQuizUseCase().execute(answered).score)
    }
}
