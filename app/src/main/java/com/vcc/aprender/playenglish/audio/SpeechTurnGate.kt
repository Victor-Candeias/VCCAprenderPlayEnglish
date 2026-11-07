package com.vcc.aprender.playenglish.audio

/**
 * Single-flight utterance coordinator, independent of Android and Compose.
 * Every engine callback must identify the active request. An old callback
 * (after stop or a newer request) cannot unlock the current utterance.
 */
class SpeechTurnGate {
    private var activeId: String? = null
    val isSpeaking: Boolean get() = activeId != null

    fun tryBegin(id: String): Boolean {
        if (id.isBlank() || activeId != null) return false
        activeId = id
        return true
    }

    /** Returns true only for the active utterance, once. */
    fun finish(id: String?): Boolean {
        if (id == null || activeId != id) return false
        activeId = null
        return true
    }

    /** Invalidate callbacks from the stopped/abandoned utterance. */
    fun cancel(): Boolean {
        val wasSpeaking = activeId != null
        activeId = null
        return wasSpeaking
    }
}
