package com.vcc.aprender.playenglish.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale
import java.util.UUID

enum class EnglishTtsStatus { INITIALIZING, READY, SPEAKING, VOICE_MISSING, ERROR, CLOSED }

/**
 * Owns the TTS engine, never an Activity. State changes are marshalled onto the
 * main thread. PE-022 uses a single-flight gate shared by all speech controls.
 */
class AndroidEnglishPronunciationOutput(
    context: Context,
    initialSpeechRate: Float = EnglishVoicePolicy.CHILD_SPEECH_RATE
) : PronunciationOutput, AutoCloseable {
    private val main = Handler(Looper.getMainLooper())
    private var engine: TextToSpeech? = null
    private val turnGate = SpeechTurnGate()
    private var closed = false
    private var speechRate = ReadingPreferences(speechRate = initialSpeechRate).normalized().speechRate

    var status by mutableStateOf(EnglishTtsStatus.INITIALIZING)
        private set

    /** A playback failure is reported without permanently disabling retries. */
    var lastPlaybackFailed by mutableStateOf(false)
        private set

    val isSpeaking: Boolean get() = turnGate.isSpeaking

    init {
        try {
            engine = TextToSpeech(context.applicationContext) { code ->
                main.post {
                    if (!closed) configureVoice(code)
                }
            }
        } catch (_: RuntimeException) {
            status = EnglishTtsStatus.ERROR
        }
    }

    private fun configureVoice(initCode: Int) {
        val tts = engine ?: run {
            status = EnglishTtsStatus.ERROR
            return
        }
        if (initCode != TextToSpeech.SUCCESS) {
            status = EnglishTtsStatus.ERROR
            return
        }
        try {
            val available = tts.isLanguageAvailable(Locale.UK)
            if (!EnglishVoicePolicy.hasCountryVoice(
                    available, TextToSpeech.LANG_COUNTRY_AVAILABLE
                )) {
                status = EnglishTtsStatus.VOICE_MISSING
                return
            }
            if (tts.setLanguage(Locale.UK) < TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                status = EnglishTtsStatus.VOICE_MISSING
                return
            }
            // Some engines silently substitute en-US or a network-only voice.
            val chosen = tts.voice
            if (chosen == null || !EnglishVoicePolicy.usableVoice(
                    chosen.locale, chosen.isNetworkConnectionRequired
                )) {
                status = EnglishTtsStatus.VOICE_MISSING
                return
            }
            tts.setSpeechRate(speechRate)
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) = releaseIfCurrent(utteranceId)
                @Deprecated("Legacy API callback")
                override fun onError(utteranceId: String?) = failIfCurrent(utteranceId)
                override fun onError(utteranceId: String?, errorCode: Int) =
                    failIfCurrent(utteranceId)
                override fun onStop(utteranceId: String?, interrupted: Boolean) =
                    releaseIfCurrent(utteranceId)
            })
            status = EnglishTtsStatus.READY
        } catch (_: RuntimeException) {
            status = EnglishTtsStatus.ERROR
        }
    }

    private fun releaseIfCurrent(id: String?) {
        main.post {
            if (!closed && turnGate.finish(id)) {
                lastPlaybackFailed = false
                status = EnglishTtsStatus.READY
            }
        }
    }

    private fun failIfCurrent(id: String?) {
        main.post {
            if (!closed && turnGate.finish(id)) {
                lastPlaybackFailed = true
                // The engine and en-GB voice are still usable: unlock retry.
                status = EnglishTtsStatus.READY
            }
        }
    }

    /** Updates an available en-GB voice, and also the rate used after async TTS init. */
    fun setSpeechRate(rate: Float) {
        if (closed) return
        speechRate = ReadingPreferences(speechRate = rate).normalized().speechRate
        if (status == EnglishTtsStatus.READY) {
            runCatching { engine?.setSpeechRate(speechRate) }
        }
    }

    override fun speak(text: String, languageTag: String): Boolean {
        if (closed || status != EnglishTtsStatus.READY ||
            languageTag != EnglishVoicePolicy.LANGUAGE_TAG || text.isBlank()) return false
        val tts = engine ?: return false
        val id = UUID.randomUUID().toString()
        if (!turnGate.tryBegin(id)) return false
        lastPlaybackFailed = false
        status = EnglishTtsStatus.SPEAKING
        return try {
            // No QUEUE_FLUSH: a second control can never interrupt speech.
            if (tts.speak(text, TextToSpeech.QUEUE_ADD, null, id) == TextToSpeech.SUCCESS) {
                true
            } else {
                failPlaybackImmediately(id)
                false
            }
        } catch (_: RuntimeException) {
            failPlaybackImmediately(id)
            false
        }
    }

    private fun failPlaybackImmediately(id: String) {
        if (turnGate.finish(id) && !closed) {
            lastPlaybackFailed = true
            status = EnglishTtsStatus.READY
        }
    }

    override fun stop() {
        val wasSpeaking = turnGate.cancel()
        if (!closed) {
            runCatching { engine?.stop() }
            if (wasSpeaking) {
                lastPlaybackFailed = false
                status = EnglishTtsStatus.READY
            }
        }
    }

    override fun close() {
        if (closed) return
        closed = true
        turnGate.cancel()
        runCatching { engine?.stop() }
        runCatching { engine?.shutdown() }
        engine = null
        status = EnglishTtsStatus.CLOSED
    }
}
