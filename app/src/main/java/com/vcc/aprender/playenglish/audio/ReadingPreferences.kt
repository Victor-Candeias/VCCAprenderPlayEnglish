package com.vcc.aprender.playenglish.audio

import android.content.Context

/**
 * Local plugin reading options only; no user profile, parent configuration,
 * network sync or host preferences are read or modified.
 */
data class ReadingPreferences(
    val audioEnabled: Boolean = true,
    val speechRate: Float = EnglishVoicePolicy.CHILD_SPEECH_RATE,
    val showInstructions: Boolean = true
) {
    fun normalized(): ReadingPreferences = copy(
        speechRate = if (speechRate.isFinite()) speechRate.coerceIn(MIN_RATE, MAX_RATE)
        else EnglishVoicePolicy.CHILD_SPEECH_RATE
    )

    companion object {
        const val MIN_RATE = 0.70f
        const val MAX_RATE = 1.10f
    }
}

/** Private application preferences, with safe defaults for new installs. */
class ReadingPreferencesStore(context: Context) {
    private val storage = context.applicationContext.getSharedPreferences(
        "pe038_reading_options", Context.MODE_PRIVATE
    )

    fun load(): ReadingPreferences = ReadingPreferences(
        audioEnabled = storage.getBoolean("audio_enabled", true),
        speechRate = storage.getFloat("speech_rate", EnglishVoicePolicy.CHILD_SPEECH_RATE),
        showInstructions = storage.getBoolean("show_instructions", true)
    ).normalized()

    fun save(options: ReadingPreferences) {
        val safe = options.normalized()
        storage.edit()
            .putBoolean("audio_enabled", safe.audioEnabled)
            .putFloat("speech_rate", safe.speechRate)
            .putBoolean("show_instructions", safe.showInstructions)
            .apply()
    }
}
