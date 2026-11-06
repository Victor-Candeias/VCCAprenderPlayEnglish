package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.domain.model.*
import com.vcc.aprender.playenglish.domain.repository.QuizContentRepository
import com.vcc.aprender.playenglish.domain.usecase.LoadQuizUseCase
import com.vcc.aprender.playenglish.ui.quiz.QuizViewModel
import org.junit.Assert.*
import org.junit.Test

class QuizViewModelTest {
    private fun source(items: List<QuizQuestion>): QuizContentRepository =
        object : QuizContentRepository {
            override fun loadQuestions(): List<QuizQuestion> = items
        }

    private fun question(id: String, correct: String = "b") = QuizQuestion(
        id, "Como se diz «Vermelho» em inglês?",
        listOf(QuizChoice("a", "Green"), QuizChoice("b", "Red"),
            QuizChoice("c", "Blue")), correct
    )

    @Test fun emptyOfflineCatalogDoesNotAllowFictitiousAnswers() {
        val vm = QuizViewModel(LoadQuizUseCase(source(emptyList())))
        assertFalse(vm.uiState.value.catalogError)
        assertFalse(vm.uiState.value.hasContent)
        assertEquals(QuizPhase.NOT_STARTED, vm.uiState.value.quiz.phase)
        assertFalse(vm.confirmOption("missing", "a"))
        vm.finishQuiz()
        assertNull(vm.uiState.value.quiz.score)
    }

    @Test fun malformedCatalogDoesNotLeakPartialState() {
        val failing = object : QuizContentRepository {
            override fun loadQuestions(): List<QuizQuestion> = error("Corrupt offline asset")
        }
        val vm = QuizViewModel(LoadQuizUseCase(failing))
        assertTrue(vm.uiState.value.catalogError)
        assertFalse(vm.uiState.value.hasContent)
        assertFalse(vm.confirmOption("q1", "a"))
    }

    @Test fun legacyDomainAnswerUseCaseRemainsImmutable() {
        val q = question("q1")
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(q))))
        val initial = vm.uiState.value
        vm.selectAnswer(q.id, "b")
        assertTrue(initial.quiz.selected.isEmpty())
        assertEquals(1, vm.uiState.value.quiz.answeredCount)
        vm.finishQuiz()
        assertEquals(QuizScore(1, 1), vm.uiState.value.quiz.score)
        vm.selectAnswer(q.id, "a")
        assertEquals(QuizScore(1, 1), vm.uiState.value.quiz.score)
    }

    @Test fun questionSetResetsPreviouslyConfirmedAttempts() {
        val first = question("quiz.colors.red")
        val second = question("quiz.colors.blue")
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(first, second))))
        vm.selectQuestionSet(listOf(first.id))
        assertEquals(listOf(first.id), vm.uiState.value.quiz.questions.map { it.id })
        assertTrue(vm.confirmOption(first.id, "a"))
        assertEquals(1, vm.uiState.value.quiz.answeredCount)
        vm.selectQuestionSet(listOf(second.id))
        assertEquals(0, vm.uiState.value.quiz.answeredCount)
        assertNull(vm.uiState.value.preview.confirmedOptionId)
        assertNull(vm.uiState.value.quiz.score)
        vm.selectQuestionSet(emptyList())
        assertFalse(vm.uiState.value.hasContent)
    }

    @Test fun invalidFilteredQuestionsFailClosed() {
        val q = question("q1")
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(q))))
        vm.selectQuestionSet(listOf("missing"))
        assertTrue(vm.uiState.value.catalogError)
        assertFalse(vm.uiState.value.hasContent)
        val vm2 = QuizViewModel(LoadQuizUseCase(source(listOf(q))))
        vm2.selectQuestionSet(listOf("q1", "q1"))
        assertTrue(vm2.uiState.value.catalogError)
    }

    @Test fun confirmOnceLocksWrongAnswerEvenIfRetriedWithCorrectChoice() {
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(question("q1")))))
        vm.selectQuestionSet(listOf("q1"))
        val presentation = vm.uiState.value.preview.question!!
        assertEquals(listOf("Green", "Red", "Blue"), presentation.options.map { it.label })
        assertFalse(presentation.javaClass.declaredFields.any {
            it.name.contains("correct", ignoreCase = true)
        })
        assertTrue(vm.confirmOption("q1", "a")) // Intentional wrong answer
        assertEquals("a", vm.uiState.value.preview.confirmedOptionId)
        assertEquals(1, vm.uiState.value.quiz.answeredCount)
        assertEquals("a", vm.uiState.value.quiz.selected["q1"])
        assertFalse(vm.confirmOption("q1", "a")) // duplicate event
        assertFalse(vm.confirmOption("q1", "b")) // correct second attempt
        assertFalse(vm.confirmOption("q1", "c")) // different card
        assertEquals(1, vm.uiState.value.quiz.answeredCount)
        assertNull(vm.uiState.value.quiz.score)
    }

    @Test fun keyboardAndTalkBackActivationsShareSameAtomicGuard() {
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(question("q1")))))
        vm.selectQuestionSet(listOf("q1"))
        assertTrue(vm.confirmOption("q1", "b"))
        repeat(30) {
            assertFalse(vm.confirmOption("q1", if (it % 2 == 0) "a" else "b"))
        }
        assertEquals(mapOf("q1" to "b"), vm.uiState.value.quiz.selected)
        assertNull(vm.uiState.value.quiz.score)
    }

    @Test fun cannotConfirmUnknownOptionOrInvisibleQuestion() {
        val first = question("q1")
        val second = question("q2")
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(first, second))))
        vm.selectQuestionSet(listOf(first.id, second.id))
        assertFalse(vm.confirmOption("q2", "a"))
        assertFalse(vm.confirmOption("q1", "not-an-option"))
        assertFalse(vm.confirmOption("missing", "b"))
        assertEquals(0, vm.uiState.value.quiz.answeredCount)
        assertNull(vm.uiState.value.preview.confirmedOptionId)
        assertFalse(vm.uiState.value.preview.canPreviewNext)
    }

    @Test fun navigationNeedsConfirmationAndCannotRepeatFinishedQuestion() {
        val first = question("q1")
        val second = question("q2")
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(first, second))))
        vm.selectQuestionSet(listOf(first.id, second.id))
        vm.nextPreviewQuestion()
        assertEquals(0, vm.uiState.value.preview.questionIndex)
        assertTrue(vm.confirmOption("q1", "b"))
        assertTrue(vm.uiState.value.preview.canPreviewNext)
        vm.nextPreviewQuestion()
        assertEquals("q2", vm.uiState.value.preview.question!!.id)
        assertFalse(vm.confirmOption("q1", "a"))
        assertFalse(vm.uiState.value.preview.isConfirmed)
        assertTrue(vm.confirmOption("q2", "c"))
        assertTrue(vm.uiState.value.preview.allAnswered)
        assertFalse(vm.uiState.value.preview.canPreviewNext)
        vm.nextPreviewQuestion()
        assertEquals(1, vm.uiState.value.preview.questionIndex)
        assertEquals(2, vm.uiState.value.quiz.answeredCount)
        assertNull(vm.uiState.value.quiz.score)
    }

    @Test fun canceledOrCompletedQuizDoesNotAcceptAnotherAttempt() {
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(question("q1")))))
        vm.selectQuestionSet(listOf("q1"))
        vm.cancelQuiz()
        assertFalse(vm.confirmOption("q1", "a"))
        assertTrue(vm.uiState.value.quiz.selected.isEmpty())

        val finished = QuizViewModel(LoadQuizUseCase(source(listOf(question("q1")))))
        finished.selectQuestionSet(listOf("q1"))
        assertTrue(finished.confirmOption("q1", "b"))
        finished.finishQuiz()
        assertEquals(QuizScore(1, 1), finished.uiState.value.quiz.score)
        assertFalse(finished.confirmOption("q1", "a"))
    }

    @Test fun correctFeedbackOnlyAfterConfirmationAndCountsOnce() {
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(question("q1")))))
        vm.selectQuestionSet(listOf("q1"))
        assertNull(vm.uiState.value.preview.wasCorrect)
        assertEquals(0, vm.uiState.value.preview.correctAnswers)
        assertFalse(vm.uiState.value.preview.canConclude)
        assertTrue(vm.confirmOption("q1", "b"))
        assertEquals(true, vm.uiState.value.preview.wasCorrect)
        assertEquals(1, vm.uiState.value.preview.correctAnswers)
        assertEquals("b", vm.uiState.value.preview.confirmedOptionId)
        assertTrue(vm.uiState.value.preview.canConclude)
        repeat(30) { assertFalse(vm.confirmOption("q1", if (it % 2 == 0) "b" else "a")) }
        assertEquals(1, vm.uiState.value.preview.correctAnswers)
        assertNull(vm.uiState.value.quiz.score)
    }

    @Test fun wrongAnswerNeverShowsSuccessOrIncrementsCorrectCount() {
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(question("q1"), question("q2")))))
        vm.selectQuestionSet(listOf("q1", "q2"))
        assertTrue(vm.confirmOption("q1", "a"))
        assertEquals(false, vm.uiState.value.preview.wasCorrect)
        assertEquals(0, vm.uiState.value.preview.correctAnswers)
        assertTrue(vm.uiState.value.preview.canPreviewNext)
        assertFalse(vm.confirmOption("q1", "b"))
        vm.nextPreviewQuestion()
        assertNull(vm.uiState.value.preview.wasCorrect)
        assertEquals(0, vm.uiState.value.preview.correctAnswers)
        assertNull(vm.uiState.value.preview.confirmedOptionId)
    }

    @Test fun smileStaysUntilAdvancingAndLastQuestionConcludesWithRealScore() {
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(question("q1"), question("q2")))))
        vm.selectQuestionSet(listOf("q1", "q2"))
        assertTrue(vm.confirmOption("q1", "b"))
        assertEquals(true, vm.uiState.value.preview.wasCorrect)
        assertEquals(true, vm.uiState.value.preview.wasCorrect)
        assertEquals(1, vm.uiState.value.preview.correctAnswers)
        assertFalse(vm.uiState.value.preview.canConclude)
        vm.finishQuiz()
        assertEquals(QuizPhase.ACTIVE, vm.uiState.value.quiz.phase)
        vm.nextPreviewQuestion()
        assertNull(vm.uiState.value.preview.wasCorrect)
        assertNull(vm.uiState.value.preview.confirmedOptionId)
        assertEquals(1, vm.uiState.value.preview.correctAnswers)
        assertTrue(vm.confirmOption("q2", "a"))
        assertEquals(false, vm.uiState.value.preview.wasCorrect)
        assertEquals(1, vm.uiState.value.preview.correctAnswers)
        assertTrue(vm.uiState.value.preview.canConclude)
        vm.finishQuiz()
        assertEquals(QuizPhase.COMPLETED, vm.uiState.value.quiz.phase)
        assertEquals(QuizScore(1, 2), vm.uiState.value.quiz.score)
        vm.finishQuiz()
        assertEquals(QuizScore(1, 2), vm.uiState.value.quiz.score)
    }

    @Test fun wrongAnswerRevealsOnlyCorrectOptionAfterConfirmationAndResetsOnNext() {
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(question("q1"), question("q2")))))
        vm.selectQuestionSet(listOf("q1", "q2"))
        assertNull(vm.uiState.value.preview.correctOptionAfterError)
        assertNull(vm.uiState.value.preview.wasCorrect)
        assertTrue(vm.confirmOption("q1", "a"))
        val wrong = vm.uiState.value.preview
        assertEquals(false, wrong.wasCorrect)
        assertEquals("b", wrong.correctOptionAfterError?.id)
        assertEquals("Red", wrong.correctOptionAfterError?.label)
        assertEquals(0, wrong.correctAnswers)
        repeat(30) { assertFalse(vm.confirmOption("q1", "b")) }
        assertEquals("b", vm.uiState.value.preview.correctOptionAfterError?.id)
        assertEquals(1, vm.uiState.value.quiz.answeredCount)
        vm.nextPreviewQuestion()
        assertNull(vm.uiState.value.preview.correctOptionAfterError)
        assertNull(vm.uiState.value.preview.wasCorrect)
        assertTrue(vm.confirmOption("q2", "b"))
        assertNull(vm.uiState.value.preview.correctOptionAfterError)
        vm.finishQuiz()
        assertEquals(QuizScore(1, 2), vm.uiState.value.quiz.score)
    }

    @Test fun changingCategoryErasesSmilesAndCorrectProgress() {
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(question("q1"), question("q2")))))
        vm.selectQuestionSet(listOf("q1"))
        assertTrue(vm.confirmOption("q1", "b"))
        assertEquals(1, vm.uiState.value.preview.correctAnswers)
        vm.selectQuestionSet(listOf("q2"))
        assertNull(vm.uiState.value.preview.wasCorrect)
        assertEquals(0, vm.uiState.value.preview.correctAnswers)
        assertFalse(vm.uiState.value.preview.isConfirmed)
        assertNull(vm.uiState.value.quiz.score)
    }
}
