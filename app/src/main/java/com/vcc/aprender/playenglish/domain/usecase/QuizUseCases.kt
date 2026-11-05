package com.vcc.aprender.playenglish.domain.usecase

import com.vcc.aprender.playenglish.domain.model.QuizPhase
import com.vcc.aprender.playenglish.domain.model.QuizQuestion
import com.vcc.aprender.playenglish.domain.model.QuizScore
import com.vcc.aprender.playenglish.domain.model.QuizState
import com.vcc.aprender.playenglish.domain.repository.QuizContentRepository

/** Returns empty NOT_STARTED until PE-012 provides real offline questions. */
class LoadQuizUseCase(private val content: QuizContentRepository) {
    fun execute(): QuizState {
        val questions: List<QuizQuestion> = content.loadQuestions().toList()
        require(questions.size <= 10_000)
        require(questions.map { it.id }.toSet().size == questions.size) {
            "Question IDs must be unique across the catalog."
        }
        return if (questions.isEmpty()) {
            QuizState()
        } else {
            QuizState(phase = QuizPhase.ACTIVE, questions = questions)
        }
    }
}

/** Wrong/duplicate/late answers leave the original state untouched. */
class AnswerQuestionUseCase {
    fun execute(state: QuizState, questionId: String, choiceId: String): QuizState {
        if (state.phase != QuizPhase.ACTIVE || questionId in state.selected) return state
        val question = state.questions.find { it.id == questionId } ?: return state
        if (question.choices.none { it.id == choiceId }) return state
        return state.copy(selected = state.selected + (questionId to choiceId))
    }
}

class CompleteQuizUseCase {
    fun execute(state: QuizState): QuizState {
        if (state.phase != QuizPhase.ACTIVE || !state.allAnswered) return state
        val correct = state.questions.count { q ->
            state.selected[q.id] == q.correctChoiceId
        }
        return state.copy(
            phase = QuizPhase.COMPLETED,
            score = QuizScore(correct, state.questions.size)
        )
    }
}

class CancelQuizUseCase {
    fun execute(state: QuizState): QuizState =
        if (state.phase == QuizPhase.ACTIVE) state.copy(phase = QuizPhase.CANCELED)
        else state
}
