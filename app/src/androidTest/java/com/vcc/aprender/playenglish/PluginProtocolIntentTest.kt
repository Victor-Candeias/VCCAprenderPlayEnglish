package com.vcc.aprender.playenglish

import android.content.ComponentName
import com.vcc.aprender.playenglish.plugin.PlayEnglishPlugin
import com.vcc.aprender.pluginsdk.GameContract
import com.vcc.aprender.pluginsdk.GameProtocol
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Wire compatibility checks; does not launch a host or bypass signature verification. */
@RunWith(AndroidJUnit4::class)
class PluginProtocolIntentTest {
    private val pluginComponent = ComponentName(
        "com.vcc.aprender.playenglish",
        "com.vcc.aprender.playenglish.MainActivity"
    )

    @Test fun launchRequestRoundTripsAsPrimitives() {
        val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 101L)
        val launch = GameProtocol.createLaunchIntent(pluginComponent, request)
        assertEquals(GameContract.ACTION_PLAY, launch.action)
        assertEquals(pluginComponent, launch.component)
        assertEquals(request, GameProtocol.readRequest(launch))
    }

    @Test fun resultRoundTripsAndRejectsTampering() {
        val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 101L)
        val result = GameProtocol.createResultIntent(request, 2, 3)
        val parsed = GameProtocol.readResult(result, request)
        assertNotNull(parsed)
        assertEquals(2, parsed?.correctAnswers)
        assertEquals(3, parsed?.totalQuestions)
        result.putExtra(GameContract.EXTRA_SESSION_ID, "tampered")
        assertNull(GameProtocol.readResult(result, request))
    }

    @Test fun rejectsUnknownProtocolAndInvalidScores() {
        val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 101L)
        val intent = GameProtocol.createLaunchIntent(pluginComponent, request)
        intent.putExtra(GameContract.EXTRA_PROTOCOL_VERSION, 2)
        assertNull(GameProtocol.readRequest(intent))
        try {
            GameProtocol.createResultIntent(request, 4, 3)
            fail("Invalid score should be rejected")
        } catch (_: IllegalArgumentException) {
            // A result must never report more correct answers than questions.
        }
    }
}
