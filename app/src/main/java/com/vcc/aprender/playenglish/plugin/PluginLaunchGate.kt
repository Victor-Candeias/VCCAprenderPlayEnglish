package com.vcc.aprender.playenglish.plugin

import com.vcc.aprender.pluginsdk.GameRequest

/**
 * Minimum fail-closed entry gate for the exported Activity.
 * More complete session lifecycle and result handling belong to PE-005/PE-006.
 */
object PluginLaunchGate {
    const val HOST_PACKAGE: String = "com.vcc.aprender"

    fun canOpen(
        callingPackage: String?,
        hasMatchingSignature: Boolean,
        request: GameRequest?
    ): Boolean =
        callingPackage == HOST_PACKAGE &&
            hasMatchingSignature &&
            PlayEnglishPlugin.acceptsValidatedRequest(request)
}
