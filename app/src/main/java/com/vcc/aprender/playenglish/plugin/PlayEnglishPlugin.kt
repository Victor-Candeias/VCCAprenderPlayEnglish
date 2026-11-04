package com.vcc.aprender.playenglish.plugin

import com.vcc.aprender.pluginsdk.GameContract
import com.vcc.aprender.pluginsdk.GameRequest
import com.vcc.aprender.pluginsdk.GameValidation

/**
 * Bridge to the vendored VCCAprender Game Plugin SDK v1.
 *
 * This is not yet an exported plugin entry point (PE-004/005).
 * A request is only eligible for further processing after
 * GameProtocol.readRequest(intent) and caller-signature validation.
 */
object PlayEnglishPlugin {
    const val GAME_ID: String = "com.vcc.aprender.playenglish"
    const val GAME_TITLE: String = "VCC Brincar ao Inglês"
    const val PROTOCOL_VERSION: Int = GameContract.PROTOCOL_VERSION

    fun acceptsValidatedRequest(request: GameRequest?): Boolean =
        request != null && GameValidation.validRequest(request) && request.gameId == GAME_ID
}
