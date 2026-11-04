package com.vcc.aprender.playenglish.plugin

import android.content.Intent
import com.vcc.aprender.pluginsdk.GameProtocol
import com.vcc.aprender.pluginsdk.GameRequest

/**
 * Serializes a real completed quiz using the immutable validated request from
 * the host. The result is emitted once and contains SDK v1 primitives only.
 *
 * The Activity calls setResult(RESULT_OK, intent) and finish() on success.
 * Normal Android back/cancel leaves RESULT_CANCELED (never 0/0).
 */
class PluginQuizResultSender(private val request: GameRequest) {
    private var resultAlreadyProduced = false

    fun createResultOnce(completion: QuizCompletion?): Intent? {
        if (resultAlreadyProduced || completion == null) return null

        val result = try {
            GameProtocol.createResultIntent(
                request = request,
                correct = completion.correctAnswers,
                total = completion.totalQuestions
            )
        } catch (_: IllegalArgumentException) {
            null
        } ?: return null

        resultAlreadyProduced = true
        return result
    }
}
