package com.vcc.aprender.playenglish.ui.quiz

import androidx.lifecycle.ViewModel
import com.vcc.aprender.playenglish.domain.model.QuizState
import com.vcc.aprender.playenglish.domain.model.QuizPhase
import com.vcc.aprender.playenglish.domain.model.QuizQuestion
import com.vcc.aprender.playenglish.domain.usecase.AnswerQuestionUseCase
import com.vcc.aprender.playenglish.domain.usecase.CancelQuizUseCase
import com.vcc.aprender.playenglish.domain.usecase.CompleteQuizUseCase
import com.vcc.aprender.playenglish.domain.usecase.LoadQuizUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Records confirmed answers exactly once in the immutable domain QuizState.
 * Gesture detection and accessibility share confirmOption(); neither has
 * a separate route to score / record duplicate answers.
 */
class QuizViewModel(
    private val loadQuiz: LoadQuizUseCase,
    private val answer: AnswerQuestionUseCase = AnswerQuestionUseCase(),
    private val complete: CompleteQuizUseCase = CompleteQuizUseCase(),
    private val cancel: CancelQuizUseCase = CancelQuizUseCase()
) : ViewModel() {
    private val mutableState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = mutableState.asStateFlow()
    private var allQuestions: List<QuizQuestion> = emptyList()

    init { reload() }

    fun reload() {
        mutableState.value = runCatching {
            val loaded = loadQuiz.execute()
            allQuestions = loaded.questions.toList()
            QuizUiState(quiz = loaded)
        }.getOrElse {
            allQuestions = emptyList()
            QuizUiState(quiz = QuizState(), catalogError = true)
        }
    }

    /**
     * Changing the category/learning level clears all prior confirmations.
     * Unknown/duplicate IDs reject the catalog without exposing partial data.
     */
    fun selectQuestionSet(ids: List<String>) {
        if (mutableState.value.catalogError) return
        val lookup = allQuestions.associateBy { it.id }
        if (ids.distinct().size != ids.size || ids.any { it !in lookup }) {
            mutableState.value = QuizUiState(catalogError = true)
            return
        }
        val questions = ids.map { checkNotNull(lookup[it]) }
        mutableState.value = QuizUiState(
            quiz = if (questions.isEmpty()) QuizState()
                   else QuizState(phase = QuizPhase.ACTIVE, questions = questions)
        )
    }

    /**
     * Returns true only for the first legitimate confirmation of the VISIBLE
     * question. Subsequent attempts (other options, second finger, keyboard,
     * duplicate events, wrong question, invalid option) return false.
     *
     * Domain answer counts are stored but NOT reported to the host yet: PE-020
     * will integrate begin/record/finish with real session results.
     */
    fun confirmOption(questionId: String, optionId: String): Boolean {
        val current = mutableState.value
        if (current.catalogError || current.quiz.phase != QuizPhase.ACTIVE) return false
        val shownQuestion = current.quiz.questions.getOrNull(current.previewIndex)
            ?: return false
        if (shownQuestion.id != questionId ||
            current.quiz.selected.containsKey(questionId) ||
            shownQuestion.choices.none { it.id == optionId }) return false

        val next = answer.execute(current.quiz, questionId, optionId)
        if (next === current.quiz || next.selected[questionId] != optionId) return false
        mutableState.value = current.copy(quiz = next)
        return true
    }

    /**
     * Only move forward after a confirmed answer. Never loop to an already
     * answered question and never skip one. Final results belong to PE-020.
     */
    fun nextPreviewQuestion() {
        val current = mutableState.value
        val shown = current.quiz.questions.getOrNull(current.previewIndex) ?: return
        if (current.catalogError || current.quiz.phase != QuizPhase.ACTIVE ||
            !current.quiz.selected.containsKey(shown.id) ||
            current.previewIndex >= current.quiz.questions.lastIndex) return
        mutableState.value = current.copy(previewIndex = current.previewIndex + 1)
    }

    /** Existing domain integration for the later PE-020 host session flow. */
    fun selectAnswer(questionId: String, choiceId: String) {
        val previous = mutableState.value
        mutableState.value = previous.copy(
            quiz = answer.execute(previous.quiz, questionId, choiceId)
        )
    }

    fun finishQuiz() {
        val previous = mutableState.value
        mutableState.value = previous.copy(quiz = complete.execute(previous.quiz))
    }

    fun cancelQuiz() {
        val previous = mutableState.value
        mutableState.value = previous.copy(quiz = cancel.execute(previous.quiz))
    }
}
