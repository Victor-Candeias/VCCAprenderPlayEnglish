package com.vcc.aprender.playenglish.plugin

import android.content.Intent
import com.vcc.aprender.pluginsdk.GameProtocol
import com.vcc.aprender.pluginsdk.GameRequest
import com.vcc.aprender.pluginsdk.GameValidation

/**
 * The minimal saved state of a host-issued session. No child personal details
 * or Activity/Intent/Parcelable objects cross the saved-state boundary.
 */
data class PluginSessionSnapshot(
    val gameId: String,
    val sessionId: String,
    val profileId: Long,
    val status: SessionStatus,
    val quiz: QuizProgressSnapshot?
)

enum class SessionStatus { ACTIVE, CANCELED, RESULT_SENT }

/**
 * Single owner of question progress and result delivery. It is recreated from
 * onSaveInstanceState after rotation/process recreation.
 *
 * The Activity's default result is RESULT_CANCELED and remains so unless a
 * finished, real quiz passes GameProtocol.createResultIntent.
 */
class PluginSessionStateHolder private constructor(
    val request: GameRequest,
    private var status: SessionStatus,
    private var quiz: QuizProgress?
) {
    private val resultSender = PluginQuizResultSender(request)

    val isActive: Boolean get() = status == SessionStatus.ACTIVE
    val isCanceled: Boolean get() = status == SessionStatus.CANCELED
    val wasResultSent: Boolean get() = status == SessionStatus.RESULT_SENT
    val answeredCount: Int get() = quiz?.answeredCount ?: 0

    /** Idempotent start: recomposition/rotation cannot reset answered questions. */
    fun beginQuiz(questionIds: List<String>): Boolean {
        if (!isActive) return false
        val existing = quiz
        if (existing != null) return existing.snapshot().questionIds == questionIds
        // Keep saved-state Bundles safely below Android's transaction limits.
        if (questionIds.size !in 1..500 ||
            questionIds.any { it.length !in 1..80 }) return false
        quiz = runCatching { QuizProgress(questionIds) }.getOrNull() ?: return false
        return true
    }

    fun recordAnswer(questionId: String, correct: Boolean): Boolean =
        isActive && quiz?.recordAnswer(questionId, correct) == true

    /**
     * Never trust a caller-provided score. Only QuizProgress, backed by real
     * questions/answers, can create a completion. Calling this twice is inert.
     */
    fun finishQuizOnce(expectedCorrect: Int? = null, expectedTotal: Int? = null): Intent? {
        if (!isActive) return null
        val progress = quiz ?: return null
        if (!progress.isComplete) return null
        // Fail closed when UI state differs from events stored for this host
        // session (for example after process restoration or duplicated input).
        if (expectedTotal != null && expectedTotal != progress.totalQuestions) return null
        if (expectedCorrect != null && expectedCorrect != progress.correctCount) return null
        val completion = progress.finishIfComplete() ?: return null
        val result = resultSender.createResultOnce(completion) ?: return null
        status = SessionStatus.RESULT_SENT
        return result
    }

    /** Cancel is terminal and cannot overwrite a result already returned. */
    fun cancel(): Boolean {
        if (!isActive) return false
        status = SessionStatus.CANCELED
        return true
    }

    fun snapshot(): PluginSessionSnapshot = PluginSessionSnapshot(
        gameId = request.gameId,
        sessionId = request.sessionId,
        profileId = request.profileId,
        status = status,
        quiz = quiz?.snapshot()
    )

    companion object {
        fun start(request: GameRequest): PluginSessionStateHolder {
            require(GameValidation.validRequest(request))
            require(request.gameId == PlayEnglishPlugin.GAME_ID)
            return PluginSessionStateHolder(request, SessionStatus.ACTIVE, null)
        }

        fun restore(request: GameRequest, snapshot: PluginSessionSnapshot): PluginSessionStateHolder? {
            if (!GameValidation.validRequest(request) ||
                request.gameId != PlayEnglishPlugin.GAME_ID ||
                snapshot.gameId != request.gameId ||
                snapshot.sessionId != request.sessionId ||
                snapshot.profileId != request.profileId) return null

            val savedQuiz = snapshot.quiz
            if (savedQuiz != null &&
                (savedQuiz.questionIds.size !in 1..500 ||
                 savedQuiz.questionIds.any { it.length !in 1..80 })) return null

            val restoredQuiz = savedQuiz?.let(QuizProgress::restore)
            if (savedQuiz != null && restoredQuiz == null) return null

            if (snapshot.status == SessionStatus.RESULT_SENT &&
                (restoredQuiz == null || !restoredQuiz.isFinalized)) return null

            // A finished quiz must never become active after recreation.
            if (snapshot.status == SessionStatus.ACTIVE &&
                restoredQuiz?.isFinalized == true) return null

            return PluginSessionStateHolder(request, snapshot.status, restoredQuiz)
        }
    }
}
