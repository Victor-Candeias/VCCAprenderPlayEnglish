package com.vcc.aprender.playenglish.ui.quiz

import com.vcc.aprender.playenglish.domain.model.QuizState

/**
 * Public presentation types deliberately do NOT contain correctChoiceId.
 * The correct answer remains exclusively in the domain/VM until PE-016+.
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
    val preselectedOptionId: String? = null
) {
    val hasQuestion: Boolean get() = question != null
    val canPreviewNext: Boolean get() = totalQuestions > 1 && hasQuestion
}

/**
 * QuizState is kept in the ViewModel for future answer validation.
 * Compose receives ONLY the sanitized QuizPreviewState projection.
 */
data class QuizUiState(
    val quiz: QuizState = QuizState(),
    val catalogError: Boolean = false,
    val previewIndex: Int = 0,
    val previewSelectedOptionId: String? = null
) {
    val hasContent: Boolean get() = quiz.questionCount > 0

    val preview: QuizPreviewState get() {
        if (catalogError) return QuizPreviewState()
        val question = quiz.questions.getOrNull(previewIndex) ?: return QuizPreviewState()
        val optionIds = question.choices.map { it.id }
        return QuizPreviewState(
            question = QuizQuestionPreview(
                id = question.id,
                prompt = question.prompt,
                options = question.choices.map { QuizOptionPreview(it.id, it.label) }
            ),
            questionIndex = previewIndex,
            totalQuestions = quiz.questionCount,
            preselectedOptionId = previewSelectedOptionId?.takeIf { it in optionIds }
        )
    }
}
