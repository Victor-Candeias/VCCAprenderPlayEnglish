package com.vcc.aprender.playenglish.audio

import java.util.Locale

/** Strict en-GB voice validation; no silent en-US/PT fallback. */
object EnglishVoicePolicy {
    const val LANGUAGE_TAG = "en-GB"
    const val CHILD_SPEECH_RATE = 0.85f

    fun validLocale(locale: Locale?): Boolean =
        locale?.language == Locale.ENGLISH.language &&
            locale.country.equals(Locale.UK.country, ignoreCase = true)

    /** Only a locally installed British-English voice qualifies. */
    fun usableVoice(locale: Locale?, requiresNetwork: Boolean): Boolean =
        validLocale(locale) && !requiresNetwork

    /** A voice advertised by the TTS engine must actually have its data installed. */
    fun usableInstalledVoice(locale: Locale?, requiresNetwork: Boolean, installed: Boolean): Boolean =
        usableVoice(locale, requiresNetwork) && installed

    fun hasCountryVoice(availability: Int, countryAvailableThreshold: Int): Boolean =
        availability >= countryAvailableThreshold
}
