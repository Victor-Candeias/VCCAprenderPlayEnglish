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
            override fun loadQuestions() = items
        }

    @Test fun emptyOfflineCatalogIsSafeForPreviewMode() {
        val vm = QuizViewModel(LoadQuizUseCase(source(emptyList())))
        assertFalse(vm.uiState.value.catalogError)
        assertFalse(vm.uiState.value.hasContent)
        assertEquals(QuizPhase.NOT_STARTED, vm.uiState.value.quiz.phase)
        vm.finishQuiz()
        assertNull(vm.uiState.value.quiz.score)
    }

    @Test fun malformedCatalogDoesNotLeakPartialState() {
        val failing = object : QuizContentRepository {
            override fun loadQuestions(): List<QuizQuestion> {
                error("Corrupt offline asset")
            }
        }
        val vm = QuizViewModel(LoadQuizUseCase(failing))
        assertTrue(vm.uiState.value.catalogError)
        assertFalse(vm.uiState.value.hasContent)
        assertNull(vm.uiState.value.quiz.score)
    }

    @Test fun viewModelUsesPureDomainAndImmutableScore() {
        val q = QuizQuestion(
            "word.red", "Qual é a tradução de vermelho?",
            listOf(QuizChoice("red", "Red"), QuizChoice("blue", "Blue"), QuizChoice("green", "Green")),
            "red"
        )
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(q))))
        val initial = vm.uiState.value
        assertTrue(initial.hasContent)
        vm.selectAnswer(q.id, "red")
        assertTrue(initial.quiz.selected.isEmpty())
        assertEquals(1, vm.uiState.value.quiz.answeredCount)
        vm.finishQuiz()
        assertEquals(QuizScore(1, 1), vm.uiState.value.quiz.score)
        vm.selectAnswer(q.id, "blue")
        assertEquals(QuizScore(1, 1), vm.uiState.value.quiz.score)
    }
}
