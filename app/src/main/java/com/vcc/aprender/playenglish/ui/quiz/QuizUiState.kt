package com.vcc.aprender.playenglish.ui.quiz

import com.vcc.aprender.playenglish.domain.model.QuizState

/**
 * Presentation-only types. A correctChoiceId is intentionally NOT exposed
 * to Compose or accessibility until PE-017/018 handle result feedback.
 */
data class QuizOptionPreview(val id: String, val label: String)

data class QuizQuestionPreview(
    val id: String,
    val prompt: String,
    val options: List<QuizOptionPreview>
) {
    init { require(options.size == 3 && options.map { it.id }.distinct().size == 3) }
}

data class QuizPreviewState(
    val question: QuizQuestionPreview? = null,
    val questionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val confirmedOptionId: String? = null,
    /** Null until answered; never reveals the correct choice ID. */
    val wasCorrect: Boolean? = null,
    /** Real count from unique confirmed answers, not UI events. */
    val correctAnswers: Int = 0,
    val allAnswered: Boolean = false
) {
    val hasQuestion: Boolean get() = question != null
    val isConfirmed: Boolean get() = confirmedOptionId != null
    val canPreviewNext: Boolean
        get() = isConfirmed && hasQuestion && questionIndex + 1 < totalQuestions
    val canConclude: Boolean
        get() = isConfirmed && allAnswered && hasQuestion &&
            questionIndex == totalQuestions - 1
}

/**
 * The full domain QuizState lives only inside the ViewModel. Compose receives
 * the sanitized preview and cannot obtain the correct answer from that type.
 */
data class QuizUiState(
    val quiz: QuizState = QuizState(),
    val catalogError: Boolean = false,
    val previewIndex: Int = 0
) {
    val hasContent: Boolean get() = quiz.questionCount > 0

    val preview: QuizPreviewState get() {
        if (catalogError) return QuizPreviewState()
        val question = quiz.questions.getOrNull(previewIndex) ?: return QuizPreviewState()
        val chosen = quiz.selected[question.id]
        return QuizPreviewState(
            question = QuizQuestionPreview(
                id = question.id,
                prompt = question.prompt,
                options = question.choices.map { QuizOptionPreview(it.id, it.label) }
            ),
            questionIndex = previewIndex,
            totalQuestions = quiz.questionCount,
            confirmedOptionId = chosen,
            wasCorrect = chosen?.let { it == question.correctChoiceId },
            correctAnswers = quiz.questions.count { q ->
                quiz.selected[q.id] == q.correctChoiceId
            },
            allAnswered = quiz.allAnswered
        )
    }
}
