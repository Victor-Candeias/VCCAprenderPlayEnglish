package com.vcc.aprender.playenglish.domain.game

/**
 * A completed non-empty quiz can produce a result for VCCAprender.
 * Answers are recorded once per known question.
 */
data class QuizCompletion internal constructor(
    val correctAnswers: Int,
    val totalQuestions: Int
) {
    init {
        require(totalQuestions in 1..10_000)
        require(correctAnswers in 0..totalQuestions)
    }
}

/** Pure, immutable snapshot; only question IDs and answer flags are stored. */
data class QuizProgressSnapshot(
    val questionIds: List<String>,
    val answers: Map<String, Boolean>,
    val finalized: Boolean
)

class QuizProgress(questionIds: List<String>) {
    private val ids = questionIds.toList()
    private val knownIds: Set<String> = ids.toSet()
    private val answered = linkedMapOf<String, Boolean>()
    private var finalized = false

    init {
        require(ids.size in 1..10_000)
        require(ids.all(String::isNotBlank))
        require(knownIds.size == ids.size) { "Question IDs must be unique" }
    }

    val answeredCount: Int get() = answered.size
    val isComplete: Boolean get() = answered.size == knownIds.size
    val isFinalized: Boolean get() = finalized

    fun recordAnswer(questionId: String, correct: Boolean): Boolean {
        if (finalized || questionId !in knownIds || questionId in answered) return false
        answered[questionId] = correct
        return true
    }

    fun finishIfComplete(): QuizCompletion? {
        if (finalized || !isComplete) return null
        val score = QuizCompletion(
            correctAnswers = answered.values.count { it },
            totalQuestions = knownIds.size
        )
        finalized = true
        return score
    }

    fun snapshot(): QuizProgressSnapshot =
        QuizProgressSnapshot(ids.toList(), answered.toMap(), finalized)

    companion object {
        /** Reject malformed snapshots; never silently change a saved score. */
        fun restore(snapshot: QuizProgressSnapshot): QuizProgress? {
            val quiz = runCatching { QuizProgress(snapshot.questionIds) }.getOrNull()
                ?: return null
            if (snapshot.answers.size > snapshot.questionIds.size) return null
            for ((id, correct) in snapshot.answers) {
                if (!quiz.recordAnswer(id, correct)) return null
            }
            if (snapshot.finalized && quiz.finishIfComplete() == null) return null
            return quiz
        }
    }
}
