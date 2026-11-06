package com.vcc.aprender.playenglish.audio

/**
 * Port for offline en-GB pronunciation. The Android TTS implementation is
 * implemented by AndroidEnglishPronunciationOutput (PE-021).
 */
interface PronunciationOutput {
    fun speak(text: String, languageTag: String = "en-GB"): Boolean
    fun stop()
}
