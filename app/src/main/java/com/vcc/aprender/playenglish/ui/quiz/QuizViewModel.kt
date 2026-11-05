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
 * MVVM coordinator. The UI observes state but never calls the host SDK.
 * Business decisions are delegated to deterministic Kotlin use cases.
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

    init {
        reload()
    }

    fun reload() {
        mutableState.value = runCatching {
            val loaded = loadQuiz.execute()
            allQuestions = loaded.questions.toList()
            QuizUiState(quiz = loaded)
        }.getOrElse {
            allQuestions = emptyList()
            // Do not display partial or untrusted catalog data on parse failure.
            QuizUiState(quiz = QuizState(), catalogError = true)
        }
    }

    /**
     * Restrict real quiz state to the IDs selected from the offline catalog.
     * Fail closed on duplicates or unknown IDs; never silently run every question.
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
     * PE-015 only: tap highlights an A/B/C card WITHOUT recording an answer.
     * The one-attempt 800 ms hold confirmation belongs to PE-016.
     * No host callbacks, score or correct-answer feedback are emitted here.
     */
    fun previewOption(questionId: String, optionId: String) {
        val current = mutableState.value
        if (current.catalogError || current.quiz.phase != QuizPhase.ACTIVE) return
        val question = current.quiz.questions.getOrNull(current.previewIndex) ?: return
        if (question.id != questionId || question.choices.none { it.id == optionId }) return
        mutableState.value = current.copy(previewSelectedOptionId = optionId)
    }

    /** Inspect the next catalog question without awarding points or answering. */
    fun nextPreviewQuestion() {
        val current = mutableState.value
        if (current.catalogError || current.quiz.questionCount <= 1) return
        mutableState.value = current.copy(
            previewIndex = (current.previewIndex + 1) % current.quiz.questionCount,
            previewSelectedOptionId = null
        )
    }

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
