package com.vcc.aprender.playenglish.plugin

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vcc.aprender.playenglish.ui.navigation.PlayEnglishNavHost
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishTheme

/**
 * Entry point reserved for VCCAprender.
 *
 * Only the framework-provided callingPackage may identify the caller.
 * Signature equality is checked via PackageManager; caller-controlled extras
 * are decoded only after both checks pass. Invalid launches cancel and close.
 *
 * The PE-005 preview intentionally never returns a score (PE-006).
 */
class PlayEnglishPluginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val caller = callingPackage
        // Check signatures only for the expected Android-provided caller.
        val sameSignature = caller == PluginLaunchGate.HOST_PACKAGE &&
            runCatching {
                packageManager.checkSignatures(
                    PluginLaunchGate.HOST_PACKAGE,
                    packageName
                ) == PackageManager.SIGNATURE_MATCH
            }.getOrDefault(false)

        val request = PluginSessionValidator.validate(
            callingPackage = caller,
            signaturesMatch = sameSignature,
            launchIntent = intent,
            expectedComponent = ComponentName(this, PlayEnglishPluginActivity::class.java)
        )
        if (request == null) {
            finish()
            return
        }

        // The opaque, validated request is intentionally not logged or exported.
        // PE-006 will use it to produce a real GameProtocol result.
        setContent {
            PlayEnglishTheme {
                PlayEnglishNavHost()
            }
        }
    }
}
