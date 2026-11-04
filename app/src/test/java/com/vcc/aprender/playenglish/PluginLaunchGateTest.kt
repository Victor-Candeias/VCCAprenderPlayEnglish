package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.plugin.PlayEnglishPlugin
import com.vcc.aprender.playenglish.plugin.PluginLaunchGate
import com.vcc.aprender.pluginsdk.GameProtocol
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PluginLaunchGateTest {
    private val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 42L)

    @Test
    fun acceptsOnlyMatchingSignatureHostAndValidRequest() {
        assertTrue(PluginLaunchGate.canOpen(
            PluginLaunchGate.HOST_PACKAGE, true, request
        ))
        assertFalse(PluginLaunchGate.canOpen(null, true, request))
        assertFalse(PluginLaunchGate.canOpen(
            "com.example.untrusted", true, request
        ))
        assertFalse(PluginLaunchGate.canOpen(
            PluginLaunchGate.HOST_PACKAGE, false, request
        ))
        assertFalse(PluginLaunchGate.canOpen(
            PluginLaunchGate.HOST_PACKAGE, true, null
        ))
    }

    @Test
    fun rejectsWrongGameIdInvalidUuidAndProfile() {
        assertFalse(PluginLaunchGate.canOpen(
            PluginLaunchGate.HOST_PACKAGE, true,
            request.copy(gameId = "com.vcc.aprender.other")
        ))
        assertFalse(PluginLaunchGate.canOpen(
            PluginLaunchGate.HOST_PACKAGE, true,
            request.copy(gameId = "com.vcc.aprender")
        ))
        assertFalse(PluginLaunchGate.canOpen(
            PluginLaunchGate.HOST_PACKAGE, true,
            request.copy(sessionId = "not-a-uuid")
        ))
        assertFalse(PluginLaunchGate.canOpen(
            PluginLaunchGate.HOST_PACKAGE, true,
            request.copy(sessionId = request.sessionId.uppercase())
        ))
        assertFalse(PluginLaunchGate.canOpen(
            PluginLaunchGate.HOST_PACKAGE, true,
            request.copy(profileId = 0L)
        ))
        assertFalse(PluginLaunchGate.canOpen(
            PluginLaunchGate.HOST_PACKAGE, true,
            request.copy(profileId = -1L)
        ))
    }
}
