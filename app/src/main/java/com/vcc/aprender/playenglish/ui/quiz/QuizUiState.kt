package com.vcc.aprender.playenglish.ui.quiz

import com.vcc.aprender.playenglish.domain.model.QuizState

/** Read-only UI projection: never contains VCCAprender session/profile data. */
data class QuizUiState(
    val quiz: QuizState = QuizState(),
    val catalogError: Boolean = false
) {
    val hasContent: Boolean get() = quiz.questionCount > 0
}
