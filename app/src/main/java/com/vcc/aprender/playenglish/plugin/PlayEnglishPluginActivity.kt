package com.vcc.aprender.playenglish.plugin

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vcc.aprender.playenglish.ui.navigation.PlayEnglishNavHost
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishTheme
import com.vcc.aprender.pluginsdk.GameProtocol

/**
 * Host-facing entry point of the separate APK.
 *
 * The manifest requires the host's signature permission. Defence in depth:
 * verify for-result caller, matching signing certificate, action, protocol,
 * game ID, opaque profile ID and session UUID before displaying any UI.
 *
 * During PE-004 the screens are previews and do not produce quiz results:
 * always return RESULT_CANCELED. PE-005/006 will own the full session/result flow.
 */
class PlayEnglishPluginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val sameSignature = runCatching {
            packageManager.checkSignatures(
                PluginLaunchGate.HOST_PACKAGE,
                packageName
            ) == PackageManager.SIGNATURE_MATCH
        }.getOrDefault(false)

        val request = runCatching { GameProtocol.readRequest(intent) }.getOrNull()
        if (!PluginLaunchGate.canOpen(callingPackage, sameSignature, request)) {
            finish()
            return
        }

        setContent {
            PlayEnglishTheme {
                PlayEnglishNavHost()
            }
        }
    }
}
