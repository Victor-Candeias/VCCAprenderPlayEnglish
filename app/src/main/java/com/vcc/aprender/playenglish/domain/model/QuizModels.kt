package com.vcc.aprender.playenglish.domain.model

/** Minimal stable offline models; PE-012 expands the vocabulary and metadata. */
data class QuizChoice(val id: String, val label: String) {
    init {
        require(id.isNotBlank())
        require(label.isNotBlank())
    }
}

data class QuizQuestion(
    val id: String,
    val prompt: String,
    val choices: List<QuizChoice>,
    val correctChoiceId: String
) {
    init {
        require(id.isNotBlank())
        require(prompt.isNotBlank())
        require(choices.size == 3) { "A pergunta exige três opções distintas." }
        require(choices.map { it.id }.toSet().size == choices.size)
        require(choices.any { it.id == correctChoiceId })
    }
}

data class QuizScore(val correctAnswers: Int, val totalQuestions: Int) {
    init {
        require(totalQuestions in 1..10_000)
        require(correctAnswers in 0..totalQuestions)
    }
}

enum class QuizPhase { NOT_STARTED, ACTIVE, COMPLETED, CANCELED }

/**
 * An immutable snapshot. This domain class has no dependency on Android,
 * Compose, the VCCAprender SDK, or the host session identifiers.
 */
data class QuizState(
    val phase: QuizPhase = QuizPhase.NOT_STARTED,
    val questions: List<QuizQuestion> = emptyList(),
    val selected: Map<String, String> = emptyMap(),
    val score: QuizScore? = null
) {
    val answeredCount: Int get() = selected.size
    val questionCount: Int get() = questions.size
    val allAnswered: Boolean
        get() = questions.isNotEmpty() && selected.size == questions.size
}
