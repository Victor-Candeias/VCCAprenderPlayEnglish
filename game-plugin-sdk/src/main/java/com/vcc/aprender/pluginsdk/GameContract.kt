package com.vcc.aprender.pluginsdk

import android.content.ComponentName
import android.content.Intent
import java.util.UUID

/**
 * Stable v1 protocol shared by VCCAprender and separately installed game APKs.
 * The APK is never loaded in the host process. Launches use explicit components.
 */
object GameContract {
    const val PROTOCOL_VERSION = 1
    const val ACTION_PLAY = "com.vcc.aprender.plugin.action.PLAY"
    const val PERMISSION_PLAY = "com.vcc.aprender.permission.PLAY_GAME"

    const val META_GAME_ID = "com.vcc.aprender.plugin.GAME_ID"
    const val META_GAME_TITLE = "com.vcc.aprender.plugin.GAME_TITLE"
    const val META_PROTOCOL_VERSION = "com.vcc.aprender.plugin.PROTOCOL_VERSION"

    const val EXTRA_PROTOCOL_VERSION = "com.vcc.aprender.plugin.extra.PROTOCOL_VERSION"
    const val EXTRA_GAME_ID = "com.vcc.aprender.plugin.extra.GAME_ID"
    const val EXTRA_SESSION_ID = "com.vcc.aprender.plugin.extra.SESSION_ID"
    const val EXTRA_PROFILE_ID = "com.vcc.aprender.plugin.extra.PROFILE_ID"
    const val EXTRA_CORRECT_ANSWERS = "com.vcc.aprender.plugin.extra.CORRECT_ANSWERS"
    const val EXTRA_TOTAL_QUESTIONS = "com.vcc.aprender.plugin.extra.TOTAL_QUESTIONS"
}

data class GameRequest(
    val gameId: String,
    val sessionId: String,
    val profileId: Long
)

data class GameResult(
    val request: GameRequest,
    val correctAnswers: Int,
    val totalQuestions: Int
)

/**
 * Pure checks: usable by JVM unit tests and before Intent/result parsing.
 * The session nonce is deliberately not an authentication secret. The signature
 * check and signature-protected Activity are the trust boundary.
 */
object GameValidation {
    private val gameIdPattern = Regex("[a-z][a-z0-9._-]{2,79}")

    fun validGameId(value: String?): Boolean =
        value != null && gameIdPattern.matches(value)

    fun validSessionId(value: String?): Boolean =
        value != null && runCatching { UUID.fromString(value).toString() == value }.getOrDefault(false)

    fun validRequest(value: GameRequest): Boolean =
        validGameId(value.gameId) && validSessionId(value.sessionId) && value.profileId > 0

    fun validResult(
        expected: GameRequest,
        gameId: String?,
        sessionId: String?,
        profileId: Long,
        protocol: Int,
        correct: Int,
        total: Int
    ): Boolean =
        validRequest(expected) &&
            protocol == GameContract.PROTOCOL_VERSION &&
            gameId == expected.gameId &&
            sessionId == expected.sessionId &&
            profileId == expected.profileId &&
            total in 1..10_000 &&
            correct in 0..total
}

/** Wire format is made of primitives only: no serialized classes or Parcelable. */
object GameProtocol {
    fun newRequest(gameId: String, profileId: Long): GameRequest =
        GameRequest(gameId, UUID.randomUUID().toString(), profileId)
            .also { require(GameValidation.validRequest(it)) }

    fun createLaunchIntent(component: ComponentName, request: GameRequest): Intent {
        require(GameValidation.validRequest(request))
        return Intent(GameContract.ACTION_PLAY)
            .setComponent(component)
            .putExtra(GameContract.EXTRA_PROTOCOL_VERSION, GameContract.PROTOCOL_VERSION)
            .putExtra(GameContract.EXTRA_GAME_ID, request.gameId)
            .putExtra(GameContract.EXTRA_SESSION_ID, request.sessionId)
            .putExtra(GameContract.EXTRA_PROFILE_ID, request.profileId)
    }

    fun readRequest(intent: Intent?): GameRequest? {
        val data = intent ?: return null
        if (data.action != GameContract.ACTION_PLAY) return null
        if (data.getIntExtra(GameContract.EXTRA_PROTOCOL_VERSION, -1) != GameContract.PROTOCOL_VERSION) return null
        val request = GameRequest(
            data.getStringExtra(GameContract.EXTRA_GAME_ID) ?: return null,
            data.getStringExtra(GameContract.EXTRA_SESSION_ID) ?: return null,
            data.getLongExtra(GameContract.EXTRA_PROFILE_ID, -1L)
        )
        return request.takeIf(GameValidation::validRequest)
    }

    fun createResultIntent(request: GameRequest, correct: Int, total: Int): Intent {
        require(GameValidation.validResult(
            request, request.gameId, request.sessionId, request.profileId,
            GameContract.PROTOCOL_VERSION, correct, total
        ))
        return Intent()
            .putExtra(GameContract.EXTRA_PROTOCOL_VERSION, GameContract.PROTOCOL_VERSION)
            .putExtra(GameContract.EXTRA_GAME_ID, request.gameId)
            .putExtra(GameContract.EXTRA_SESSION_ID, request.sessionId)
            .putExtra(GameContract.EXTRA_PROFILE_ID, request.profileId)
            .putExtra(GameContract.EXTRA_CORRECT_ANSWERS, correct)
            .putExtra(GameContract.EXTRA_TOTAL_QUESTIONS, total)
    }

    fun readResult(data: Intent?, expected: GameRequest): GameResult? {
        if (data == null) return null
        val correct = data.getIntExtra(GameContract.EXTRA_CORRECT_ANSWERS, -1)
        val total = data.getIntExtra(GameContract.EXTRA_TOTAL_QUESTIONS, -1)
        if (!GameValidation.validResult(
            expected,
            data.getStringExtra(GameContract.EXTRA_GAME_ID),
            data.getStringExtra(GameContract.EXTRA_SESSION_ID),
            data.getLongExtra(GameContract.EXTRA_PROFILE_ID, -1L),
            data.getIntExtra(GameContract.EXTRA_PROTOCOL_VERSION, -1),
            correct, total
        )) return null
        return GameResult(expected, correct, total)
    }
}
