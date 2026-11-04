package com.vcc.aprender.playenglish.plugin

import android.content.ComponentName
import android.content.Intent
import com.vcc.aprender.pluginsdk.GameProtocol
import com.vcc.aprender.pluginsdk.GameRequest

/**
 * Fail-closed session boundary for the plugin's exported Activity.
 *
 * The caller identity must come from Activity.callingPackage, never from extras.
 * The signature flag must come from PackageManager.checkSignatures.
 * The host v1 SDK always supplies an explicit target ComponentName.
 */
object PluginSessionValidator {
    fun validate(
        callingPackage: String?,
        signaturesMatch: Boolean,
        launchIntent: Intent?,
        expectedComponent: ComponentName
    ): GameRequest? {
        // Reject unknown or unsigned callers *before* unmarshalling input.
        if (callingPackage != PluginLaunchGate.HOST_PACKAGE || !signaturesMatch) {
            return null
        }

        // Reject implicit launches or explicit launches to a different Activity.
        val incoming = launchIntent ?: return null
        if (incoming.component != expectedComponent) return null

        // GameProtocol checks ACTION_PLAY, protocol v1, gameId/nonce/profile ID
        // types and ranges. Malformed Intent extras must never crash the Activity.
        val request = try {
            GameProtocol.readRequest(incoming)
        } catch (_: RuntimeException) {
            null
        }

        return request?.takeIf {
            PluginLaunchGate.canOpen(callingPackage, signaturesMatch, it)
        }
    }
}
