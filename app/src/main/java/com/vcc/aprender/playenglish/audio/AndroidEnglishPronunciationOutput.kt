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
 * main thread. PE-022 will extend coordination between multiple speech buttons.
 */
class AndroidEnglishPronunciationOutput(context: Context) : PronunciationOutput, AutoCloseable {
    private val main = Handler(Looper.getMainLooper())
    private var engine: TextToSpeech? = null
    private var activeUtteranceId: String? = null
    private var closed = false

    var status by mutableStateOf(EnglishTtsStatus.INITIALIZING)
        private set

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
            tts.setSpeechRate(EnglishVoicePolicy.CHILD_SPEECH_RATE)
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
            if (!closed && id != null && id == activeUtteranceId) {
                activeUtteranceId = null
                status = EnglishTtsStatus.READY
            }
        }
    }

    private fun failIfCurrent(id: String?) {
        main.post {
            if (!closed && id != null && id == activeUtteranceId) {
                activeUtteranceId = null
                status = EnglishTtsStatus.ERROR
            }
        }
    }

    override fun speak(text: String, languageTag: String): Boolean {
        if (closed || status != EnglishTtsStatus.READY ||
            languageTag != EnglishVoicePolicy.LANGUAGE_TAG || text.isBlank()) return false
        val tts = engine ?: return false
        val id = UUID.randomUUID().toString()
        return try {
            activeUtteranceId = id
            status = EnglishTtsStatus.SPEAKING
            if (tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) == TextToSpeech.SUCCESS) {
                true
            } else {
                activeUtteranceId = null
                status = EnglishTtsStatus.ERROR
                false
            }
        } catch (_: RuntimeException) {
            activeUtteranceId = null
            status = EnglishTtsStatus.ERROR
            false
        }
    }

    override fun stop() {
        activeUtteranceId = null
        if (!closed) {
            runCatching { engine?.stop() }
            if (status == EnglishTtsStatus.SPEAKING) status = EnglishTtsStatus.READY
        }
    }

    override fun close() {
        if (closed) return
        closed = true
        activeUtteranceId = null
        runCatching { engine?.stop() }
        runCatching { engine?.shutdown() }
        engine = null
        status = EnglishTtsStatus.CLOSED
    }
}
