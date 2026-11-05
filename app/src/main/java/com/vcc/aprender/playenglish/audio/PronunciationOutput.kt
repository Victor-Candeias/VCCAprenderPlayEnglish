package com.vcc.aprender.playenglish.audio

/**
 * Port for offline en-GB pronunciation. The Android TTS implementation is
 * deliberately deferred to the pronunciation/audio issue.
 */
interface PronunciationOutput {
    fun speak(text: String, languageTag: String = "en-GB"): Boolean
    fun stop()
}
