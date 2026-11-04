package com.vcc.aprender.playenglish.plugin

/**
 * Only a *finished*, non-empty quiz may produce a result for VCCAprender.
 * The score is based on actual answers to a pre-selected set of question IDs.
 */
data class QuizCompletion internal constructor(
    val correctAnswers: Int,
    val totalQuestions: Int
) {
    init {
        require(totalQuestions in 1..10_000) { "Quiz must have 1..10000 questions" }
        require(correctAnswers in 0..totalQuestions) { "Invalid number of correct answers" }
    }
}

/**
 * Records a single answer per known question. Unknown/repeated answers do not
 * alter the score. PE-015 will call recordAnswer from the real quiz UI.
 *
 * No names, ages or child information are accepted or stored.
 */
class QuizProgress(questionIds: List<String>) {
    private val knownQuestionIds: Set<String> = questionIds.toSet()
    private val answered = mutableMapOf<String, Boolean>()
    private var finalized = false

    init {
        require(questionIds.size in 1..10_000)
        require(questionIds.all(String::isNotBlank))
        require(knownQuestionIds.size == questionIds.size) { "Question IDs must be unique" }
    }

    val answeredCount: Int
        get() = answered.size

    val isComplete: Boolean
        get() = answered.size == knownQuestionIds.size

    fun recordAnswer(questionId: String, correct: Boolean): Boolean {
        if (finalized || questionId !in knownQuestionIds || questionId in answered) return false
        answered[questionId] = correct
        return true
    }

    /**
     * Consumes the completed quiz exactly once. Incomplete or already-consumed
     * sessions can never generate a score (including 0/0).
     */
    fun finishIfComplete(): QuizCompletion? {
        if (finalized || !isComplete) return null
        val score = QuizCompletion(
            correctAnswers = answered.values.count { it },
            totalQuestions = knownQuestionIds.size
        )
        finalized = true
        return score
    }
}
