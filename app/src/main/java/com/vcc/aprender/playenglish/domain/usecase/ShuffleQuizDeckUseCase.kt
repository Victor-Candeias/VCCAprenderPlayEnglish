package com.vcc.aprender.playenglish.domain.usecase

import com.vcc.aprender.playenglish.domain.model.QuizQuestion
import kotlin.random.Random

/**
 * PE-019: offline, reproducible shuffle of a filtered quiz deck.
 * Stable question/choice IDs are never rewritten, so scoring and the
 * one-attempt guard remain independent from A/B/C visual positions.
 */
class ShuffleQuizDeckUseCase {
    fun execute(questions: List<QuizQuestion>, seed: Long): List<QuizQuestion> {
        require(questions.map { it.id }.distinct().size == questions.size) {
            "Question IDs must be unique in a game."
        }
        // An identical label must never be offered as both answer and distractor.
        require(questions.all { q ->
            q.choices.map { it.label.trim().lowercase() }.distinct().size == 3
        }) { "A question must contain three unambiguous answer labels." }
        val random = Random(seed)
        val order = questions.shuffled(random)
        // Balanced, randomly rotated A/B/C correct slots, even for small decks.
        val slots = listOf(0, 1, 2).shuffled(random)
        return order.mapIndexed { index, question ->
            val correct = question.choices.single { it.id == question.correctChoiceId }
            val wrong = question.choices.filter { it.id != question.correctChoiceId }
                .shuffled(random).toMutableList()
            wrong.add(slots[index % 3], correct)
            question.copy(choices = wrong)
        }
    }
}
