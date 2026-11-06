package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.domain.model.QuizChoice
import com.vcc.aprender.playenglish.domain.model.QuizQuestion
import com.vcc.aprender.playenglish.domain.usecase.ShuffleQuizDeckUseCase
import com.vcc.aprender.playenglish.domain.usecase.LoadQuizUseCase
import com.vcc.aprender.playenglish.domain.repository.QuizContentRepository
import com.vcc.aprender.playenglish.ui.quiz.QuizViewModel
import org.junit.Assert.*
import org.junit.Test

class ShuffleQuizDeckUseCaseTest {
    private val source = (1..17).map { index ->
        QuizQuestion("q$index", "Pergunta $index",
            listOf(QuizChoice("a", "Dog $index"),
                QuizChoice("b", "Cat $index"), QuizChoice("c", "Bird $index")), "b")
    }
    private val shuffle = ShuffleQuizDeckUseCase()

    @Test fun sameSeedReproducesQuestionOrderAndOptionOrder() {
        assertEquals(shuffle.execute(source, 321L), shuffle.execute(source, 321L))
        assertNotEquals(shuffle.execute(source, 321L), shuffle.execute(source, 322L))
    }

    @Test fun everyQuestionAppearsOnceAndCorrectAnswerSurvivesReordering() {
        val deck = shuffle.execute(source, 42L)
        assertEquals(source.map { it.id }.toSet(), deck.map { it.id }.toSet())
        assertEquals(source.size, deck.size)
        deck.forEach { q ->
            val original = source.single { it.id == q.id }
            assertEquals(original.choices.toSet(), q.choices.toSet())
            assertEquals(original.correctChoiceId, q.correctChoiceId)
            assertEquals(3, q.choices.map { it.label }.distinct().size)
        }
    }

    @Test fun rightAnswersBalancedAcrossABCSlots() {
        val deck = shuffle.execute(source, 10L)
        val counts = (0..2).map { position ->
            deck.count { it.choices[position].id == it.correctChoiceId }
        }
        assertEquals(1, counts.maxOrNull()!! - counts.minOrNull()!!)
    }

    @Test fun ambiguousDuplicateAnswerLabelsAreRejected() {
        val question = QuizQuestion("x", "Translate",
            listOf(QuizChoice("a", "Cat"), QuizChoice("b", " cat "),
                QuizChoice("c", "Bird")), "a")
        try {
            shuffle.execute(listOf(question), 1L)
            fail("Should reject ambiguous labels")
        } catch (expected: IllegalArgumentException) { }
    }

    @Test fun viewModelSeedKeepsSingleAttemptAndCorrectScore() {
        val content = object : QuizContentRepository {
            override fun loadQuestions() = source
        }
        val vm = QuizViewModel(LoadQuizUseCase(content))
        vm.selectQuestionSet(source.map { it.id }, 44L)
        val deck = vm.uiState.value.quiz.questions
        deck.forEachIndexed { position, q ->
            assertEquals(q.id, vm.uiState.value.preview.question?.id)
            assertTrue(vm.confirmOption(q.id, q.correctChoiceId))
            assertFalse(vm.confirmOption(q.id, q.correctChoiceId))
            if (position < deck.lastIndex) vm.nextPreviewQuestion()
        }
        assertEquals(source.size, vm.uiState.value.preview.correctAnswers)
        vm.finishQuiz()
        assertEquals(source.size, vm.uiState.value.quiz.score?.correctAnswers)
    }
}
