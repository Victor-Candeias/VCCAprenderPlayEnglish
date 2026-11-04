package com.vcc.aprender.pluginsdk

import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class GameValidationTest {
    private val expected = GameRequest("com.vcc.aprender.demo", UUID.randomUUID().toString(), 42L)

    @Test fun acceptsExactValidResult() {
        assertTrue(GameValidation.validResult(
            expected, expected.gameId, expected.sessionId, 42L, 1, 2, 3
        ))
    }

    @Test fun rejectsSpoofedGameProfileOrNonce() {
        assertFalse(GameValidation.validResult(expected, "com.other.game", expected.sessionId, 42L, 1, 2, 3))
        assertFalse(GameValidation.validResult(expected, expected.gameId, UUID.randomUUID().toString(), 42L, 1, 2, 3))
        assertFalse(GameValidation.validResult(expected, expected.gameId, expected.sessionId, 99L, 1, 2, 3))
    }

    @Test fun rejectsIncompatibleProtocolAndInvalidScores() {
        assertFalse(GameValidation.validResult(expected, expected.gameId, expected.sessionId, 42L, 2, 2, 3))
        assertFalse(GameValidation.validResult(expected, expected.gameId, expected.sessionId, 42L, 1, -1, 3))
        assertFalse(GameValidation.validResult(expected, expected.gameId, expected.sessionId, 42L, 1, 4, 3))
        assertFalse(GameValidation.validResult(expected, expected.gameId, expected.sessionId, 42L, 1, 1, 0))
        assertFalse(GameValidation.validResult(expected, expected.gameId, expected.sessionId, 42L, 1, 1, 10001))
    }

    @Test fun rejectsBadIdentifiers() {
        assertFalse(GameValidation.validGameId("../etc/passwd"))
        assertFalse(GameValidation.validGameId("x"))
        assertFalse(GameValidation.validSessionId("not-a-uuid"))
        assertFalse(GameValidation.validRequest(expected.copy(profileId = 0L)))
        assertTrue(GameValidation.validRequest(expected))
    }
}
