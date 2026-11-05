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
    @Test fun selectionRestrictsActualQuizStateAndResetsAnswers() {
        val first = QuizQuestion(
            "quiz.animals.cat", "Como se diz Gato?",
            listOf(QuizChoice("animals.cat", "Cat"), QuizChoice("animals.dog", "Dog"),
                QuizChoice("animals.bird", "Bird")), "animals.cat"
        )
        val second = QuizQuestion(
            "quiz.colors.red", "Como se diz Vermelho?",
            listOf(QuizChoice("colors.red", "Red"), QuizChoice("colors.blue", "Blue"),
                QuizChoice("colors.green", "Green")), "colors.red"
        )
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(first, second))))
        vm.selectQuestionSet(listOf(first.id))
        assertEquals(listOf(first.id), vm.uiState.value.quiz.questions.map { it.id })
        vm.selectAnswer(first.id, "animals.cat")
        assertEquals(1, vm.uiState.value.quiz.answeredCount)
        vm.selectQuestionSet(listOf(second.id))
        assertEquals(listOf(second.id), vm.uiState.value.quiz.questions.map { it.id })
        assertEquals(0, vm.uiState.value.quiz.answeredCount)
        assertNull(vm.uiState.value.quiz.score)
        vm.selectQuestionSet(emptyList())
        assertFalse(vm.uiState.value.hasContent)
        assertEquals(QuizPhase.NOT_STARTED, vm.uiState.value.quiz.phase)
    }

    @Test fun unknownOrDuplicateIdsFailClosed() {
        val first = QuizQuestion("q1", "Pergunta",
            listOf(QuizChoice("a", "A"), QuizChoice("b", "B"), QuizChoice("c", "C")), "a")
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(first))))
        vm.selectQuestionSet(listOf("missing"))
        assertTrue(vm.uiState.value.catalogError)
        assertFalse(vm.uiState.value.hasContent)

        val anotherVm = QuizViewModel(LoadQuizUseCase(source(listOf(first))))
        anotherVm.selectQuestionSet(listOf("q1", "q1"))
        assertTrue(anotherVm.uiState.value.catalogError)
    }

    @Test fun previewChoiceIsInteractiveButDoesNotAnswerOrAwardPoints() {
        val question = QuizQuestion(
            "quiz.colors.red", "Qual é a tradução de Vermelho?",
            listOf(QuizChoice("colors.green", "Green"),
                QuizChoice("colors.red", "Red"), QuizChoice("colors.blue", "Blue")),
            "colors.red"
        )
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(question))))
        vm.selectQuestionSet(listOf(question.id))
        assertNull(vm.uiState.value.preview.preselectedOptionId)
        assertEquals(listOf("Green", "Red", "Blue"),
            vm.uiState.value.preview.question!!.options.map { it.label })
        vm.previewOption(question.id, "colors.red")
        assertEquals("colors.red", vm.uiState.value.preview.preselectedOptionId)
        assertTrue(vm.uiState.value.quiz.selected.isEmpty())
        assertEquals(QuizPhase.ACTIVE, vm.uiState.value.quiz.phase)
        assertNull(vm.uiState.value.quiz.score)
        vm.previewOption(question.id, "colors.green")
        assertEquals("colors.green", vm.uiState.value.preview.preselectedOptionId)
        assertTrue(vm.uiState.value.quiz.selected.isEmpty())
    }

    @Test fun previewRejectsUnknownOptionsAndDoesNotRevealCorrectAnswer() {
        val q = QuizQuestion("quiz.colors.red", "Vermelho?",
            listOf(QuizChoice("a", "Green"), QuizChoice("b", "Red"),
                QuizChoice("c", "Blue")), "b")
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(q))))
        vm.selectQuestionSet(listOf(q.id))
        vm.previewOption("wrong-question", "b")
        vm.previewOption(q.id, "fake")
        assertNull(vm.uiState.value.preview.preselectedOptionId)
        val preview = vm.uiState.value.preview.question!!
        assertEquals(q.prompt, preview.prompt)
        assertEquals(3, preview.options.size)
        assertFalse(preview.javaClass.declaredFields.any {
            it.name.contains("correct", ignoreCase = true)
        })
        assertFalse(vm.uiState.value.preview.javaClass.declaredFields.any {
            it.name.contains("correct", ignoreCase = true)
        })
    }

    @Test fun nextPreviewResetsChoiceAndCategorySelectionStartsFromFirstQuestion() {
        val first = QuizQuestion("q1", "Animal?",
            listOf(QuizChoice("a", "Cat"), QuizChoice("b", "Dog"),
                QuizChoice("c", "Bird")), "a")
        val second = QuizQuestion("q2", "Cor?",
            listOf(QuizChoice("r", "Red"), QuizChoice("g", "Green"),
                QuizChoice("b", "Blue")), "r")
        val vm = QuizViewModel(LoadQuizUseCase(source(listOf(first, second))))
        vm.selectQuestionSet(listOf(first.id, second.id))
        vm.previewOption(first.id, "a")
        vm.nextPreviewQuestion()
        assertEquals(1, vm.uiState.value.preview.questionIndex)
        assertEquals(second.id, vm.uiState.value.preview.question!!.id)
        assertNull(vm.uiState.value.preview.preselectedOptionId)
        vm.previewOption(second.id, "r")
        vm.nextPreviewQuestion()
        assertEquals(0, vm.uiState.value.preview.questionIndex)
        assertNull(vm.uiState.value.preview.preselectedOptionId)
        assertTrue(vm.uiState.value.quiz.selected.isEmpty())
        vm.selectQuestionSet(listOf(second.id))
        assertEquals(0, vm.uiState.value.preview.questionIndex)
        assertEquals(second.id, vm.uiState.value.preview.question!!.id)
        assertNull(vm.uiState.value.preview.preselectedOptionId)
    }

}
