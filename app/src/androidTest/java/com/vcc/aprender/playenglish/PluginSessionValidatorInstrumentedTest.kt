package com.vcc.aprender.playenglish

import android.content.ComponentName
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vcc.aprender.playenglish.plugin.PlayEnglishPlugin
import com.vcc.aprender.playenglish.plugin.PlayEnglishPluginActivity
import com.vcc.aprender.playenglish.plugin.PluginLaunchGate
import com.vcc.aprender.playenglish.plugin.PluginSessionValidator
import com.vcc.aprender.pluginsdk.GameContract
import com.vcc.aprender.pluginsdk.GameProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Android Intent parsing tests: runs on emulator/device.
 * The CI compiles these tests but does not execute an emulator.
 */
@RunWith(AndroidJUnit4::class)
class PluginSessionValidatorInstrumentedTest {
    private val component = ComponentName(
        PlayEnglishPlugin.GAME_ID,
        PlayEnglishPluginActivity::class.java.name
    )
    private val request = GameProtocol.newRequest(PlayEnglishPlugin.GAME_ID, 87L)
    private val validIntent: Intent
        get() = GameProtocol.createLaunchIntent(component, request)

    private fun validate(
        intent: Intent?,
        caller: String? = PluginLaunchGate.HOST_PACKAGE,
        trusted: Boolean = true,
        target: ComponentName = component
    ) = PluginSessionValidator.validate(caller, trusted, intent, target)

    @Test
    fun validHostSignedExplicitV1RequestIsAccepted() {
        assertEquals(request, validate(validIntent))
    }

    @Test
    fun onlySystemReportedHostPackageAndMatchingCertificateAreAccepted() {
        assertNull(validate(validIntent, caller = null))
        assertNull(validate(validIntent, caller = "com.example.fake"))
        assertNull(validate(validIntent, trusted = false))
        assertNull(validate(null))
    }

    @Test
    fun requiresExactExplicitPluginActivityComponentAndAction() {
        assertNull(validate(Intent(validIntent).setComponent(null)))
        assertNull(validate(
            Intent(validIntent).setComponent(
                ComponentName(PlayEnglishPlugin.GAME_ID, "com.vcc.aprender.playenglish.MainActivity")
            )
        ))
        assertNull(validate(validIntent, target = ComponentName(
            PlayEnglishPlugin.GAME_ID, "com.vcc.aprender.playenglish.MainActivity"
        )))
        assertNull(validate(Intent(validIntent).setAction(Intent.ACTION_MAIN)))
        assertNull(validate(Intent(validIntent).setAction(null)))
    }

    @Test
    fun rejectsWrongVersionOrMissingRequiredExtras() {
        assertNull(validate(Intent(validIntent).apply {
            putExtra(GameContract.EXTRA_PROTOCOL_VERSION, 2)
        }))
        assertNull(validate(Intent(validIntent).apply {
            removeExtra(GameContract.EXTRA_PROTOCOL_VERSION)
        }))
        assertNull(validate(Intent(validIntent).apply {
            removeExtra(GameContract.EXTRA_GAME_ID)
        }))
        assertNull(validate(Intent(validIntent).apply {
            removeExtra(GameContract.EXTRA_SESSION_ID)
        }))
        assertNull(validate(Intent(validIntent).apply {
            removeExtra(GameContract.EXTRA_PROFILE_ID)
        }))
    }

    @Test
    fun rejectsWrongGameSessionOrProfileValues() {
        assertNull(validate(Intent(validIntent).apply {
            putExtra(GameContract.EXTRA_GAME_ID, "com.vcc.aprender.other")
        }))
        assertNull(validate(Intent(validIntent).apply {
            putExtra(GameContract.EXTRA_SESSION_ID, "not-a-uuid")
        }))
        assertNull(validate(Intent(validIntent).apply {
            putExtra(GameContract.EXTRA_SESSION_ID, request.sessionId.uppercase())
        }))
        assertNull(validate(Intent(validIntent).apply {
            putExtra(GameContract.EXTRA_PROFILE_ID, 0L)
        }))
        assertNull(validate(Intent(validIntent).apply {
            putExtra(GameContract.EXTRA_PROFILE_ID, -1L)
        }))
    }

    @Test
    fun mismatchedExtraTypesFailClosedWithoutCrashing() {
        assertNull(validate(Intent(validIntent).apply {
            putExtra(GameContract.EXTRA_PROTOCOL_VERSION, "1")
        }))
        assertNull(validate(Intent(validIntent).apply {
            putExtra(GameContract.EXTRA_GAME_ID, 12)
        }))
        assertNull(validate(Intent(validIntent).apply {
            putExtra(GameContract.EXTRA_SESSION_ID, 12)
        }))
        assertNull(validate(Intent(validIntent).apply {
            putExtra(GameContract.EXTRA_PROFILE_ID, "87")
        }))
    }
}
