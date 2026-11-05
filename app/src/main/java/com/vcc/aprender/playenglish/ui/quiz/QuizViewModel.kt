package com.vcc.aprender.playenglish.ui.quiz

import androidx.lifecycle.ViewModel
import com.vcc.aprender.playenglish.domain.model.QuizState
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

    init {
        reload()
    }

    fun reload() {
        mutableState.value = runCatching {
            QuizUiState(quiz = loadQuiz.execute())
        }.getOrElse {
            // Do not display partial or untrusted catalog data on parse failure.
            QuizUiState(quiz = QuizState(), catalogError = true)
        }
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
