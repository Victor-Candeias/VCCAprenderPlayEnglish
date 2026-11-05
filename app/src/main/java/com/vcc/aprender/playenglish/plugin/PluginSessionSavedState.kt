package com.vcc.aprender.playenglish.plugin

import android.os.Bundle
import com.vcc.aprender.pluginsdk.GameRequest

/**
 * Bundle conversion for Activity.onSaveInstanceState. Only framework-owned
 * process state is accepted; the host's original Intent is still revalidated
 * before restore. Corrupt/missing state fails closed.
 */
internal object PluginSessionSavedState {
    private const val ROOT = "com.vcc.aprender.playenglish.SESSION_STATE"
    private const val FORMAT = "version"
    private const val GAME = "game_id"
    private const val SESSION = "session_id"
    private const val PROFILE = "profile_id"
    private const val STATUS = "status"
    private const val HAS_QUIZ = "has_quiz"
    private const val QUESTIONS = "questions"
    private const val ANSWER_IDS = "answer_ids"
    private const val ANSWER_VALUES = "answer_values"
    private const val FINALIZED = "finalized"
    private const val FORMAT_VERSION = 1

    fun write(outState: Bundle, state: PluginSessionSnapshot) {
        val nested = Bundle().apply {
            putInt(FORMAT, FORMAT_VERSION)
            putString(GAME, state.gameId)
            putString(SESSION, state.sessionId)
            putLong(PROFILE, state.profileId)
            putString(STATUS, state.status.name)
            val quiz = state.quiz
            putBoolean(HAS_QUIZ, quiz != null)
            if (quiz != null) {
                putStringArrayList(QUESTIONS, ArrayList(quiz.questionIds))
                putStringArrayList(ANSWER_IDS, ArrayList(quiz.answers.keys))
                putBooleanArray(ANSWER_VALUES, quiz.answers.values.toBooleanArray())
                putBoolean(FINALIZED, quiz.finalized)
            }
        }
        outState.putBundle(ROOT, nested)
    }

    @Suppress("DEPRECATION")
    fun read(savedState: Bundle, request: GameRequest): PluginSessionSnapshot? =
        try {
            val b = savedState.getBundle(ROOT) ?: return null
            if (b.getInt(FORMAT, -1) != FORMAT_VERSION ||
                !b.containsKey(GAME) || !b.containsKey(SESSION) ||
                !b.containsKey(PROFILE) || !b.containsKey(STATUS) ||
                !b.containsKey(HAS_QUIZ)) return null
            val gameId = b.getString(GAME) ?: return null
            val sessionId = b.getString(SESSION) ?: return null
            val profileId = b.getLong(PROFILE)
            if (gameId != request.gameId ||
                sessionId != request.sessionId ||
                profileId != request.profileId) return null

            val status = SessionStatus.values().firstOrNull {
                it.name == b.getString(STATUS)
            } ?: return null

            val quiz = if (b.getBoolean(HAS_QUIZ)) {
                val questions = b.getStringArrayList(QUESTIONS)?.toList() ?: return null
                val ids = b.getStringArrayList(ANSWER_IDS)?.toList() ?: return null
                val values = b.getBooleanArray(ANSWER_VALUES) ?: return null
                if (ids.size != values.size ||
                    ids.size > questions.size ||
                    ids.toSet().size != ids.size) return null
                QuizProgressSnapshot(
                    questionIds = questions,
                    answers = ids.indices.associate { ids[it] to values[it] },
                    finalized = b.getBoolean(FINALIZED, false)
                )
            } else {
                // Reject inconsistent state instead of silently discarding it.
                if (b.containsKey(QUESTIONS) || b.containsKey(ANSWER_IDS) ||
                    b.containsKey(ANSWER_VALUES) || b.containsKey(FINALIZED)) return null
                null
            }
            PluginSessionSnapshot(gameId, sessionId, profileId, status, quiz)
                .takeIf { PluginSessionStateHolder.restore(request, it) != null }
        } catch (_: RuntimeException) {
            null
        }
}
