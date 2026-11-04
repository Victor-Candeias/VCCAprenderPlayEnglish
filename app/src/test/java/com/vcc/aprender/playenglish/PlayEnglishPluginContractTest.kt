package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.plugin.PlayEnglishPlugin
import com.vcc.aprender.pluginsdk.GameContract
import com.vcc.aprender.pluginsdk.GameProtocol
import com.vcc.aprender.pluginsdk.GameRequest
import com.vcc.aprender.pluginsdk.GameValidation
import org.junit.Assert.*
import org.junit.Test

class PlayEnglishPluginContractTest {
    @Test fun sdkMatchesHostProtocolVersionOne() {
        assertEquals(1, GameContract.PROTOCOL_VERSION)
        assertEquals(GameContract.PROTOCOL_VERSION, PlayEnglishPlugin.PROTOCOL_VERSION)
        assertEquals("com.vcc.aprender.plugin.action.PLAY", GameContract.ACTION_PLAY)
        assertEquals("com.vcc.aprender.permission.PLAY_GAME", GameContract.PERMISSION_PLAY)
    }

    @Test fun gameIdentityIsValidAndIndependentOfHost() {
        assertEquals("com.vcc.aprender.playenglish", PlayEnglishPlugin.GAME_ID)
        assertTrue(GameValidation.validGameId(PlayEnglishPlugin.GAME_ID))
        assertNotEquals("com.vcc.aprender", PlayEnglishPlugin.GAME_ID)
    }

    @Test fun bridgeAcceptsMatchingRequestOnly() {
        val valid = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 101)
        assertTrue(PlayEnglishPlugin.acceptsValidatedRequest(valid))
        assertFalse(PlayEnglishPlugin.acceptsValidatedRequest(null))
        assertFalse(PlayEnglishPlugin.acceptsValidatedRequest(valid.copy(gameId = "com.vcc.aprender.sample.quiz")))
        assertFalse(PlayEnglishPlugin.acceptsValidatedRequest(valid.copy(profileId = 0)))
        assertFalse(PlayEnglishPlugin.acceptsValidatedRequest(
            GameRequest(PlayEnglishPlugin.GAME_ID, "invalid", 101)
        ))
    }

    @Test fun validationRejectsIncompatibleOrTamperedScores() {
        val valid = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 101)
        assertTrue(GameValidation.validResult(
            valid, valid.gameId, valid.sessionId, valid.profileId, 1, 2, 3
        ))
        assertFalse(GameValidation.validResult(
            valid, valid.gameId, valid.sessionId, valid.profileId, 2, 2, 3
        ))
        assertFalse(GameValidation.validResult(
            valid, valid.gameId, valid.sessionId, valid.profileId, 1, 4, 3
        ))
    }
}
