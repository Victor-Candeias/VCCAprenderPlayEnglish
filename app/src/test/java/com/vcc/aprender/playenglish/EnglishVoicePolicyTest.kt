package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.audio.EnglishVoicePolicy
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class EnglishVoicePolicyTest {
    @Test fun onlyBritishEnglishLocaleIsAccepted() {
        assertTrue(EnglishVoicePolicy.validLocale(Locale.UK))
        assertTrue(EnglishVoicePolicy.validLocale(Locale.forLanguageTag("en-GB")))
        assertFalse(EnglishVoicePolicy.validLocale(Locale.US))
        assertFalse(EnglishVoicePolicy.validLocale(Locale("pt", "PT")))
        assertFalse(EnglishVoicePolicy.validLocale(null))
    }

    @Test fun onlineVoiceIsRejectedEvenWhenBritishEnglish() {
        assertTrue(EnglishVoicePolicy.usableVoice(Locale.UK, false))
        assertFalse(EnglishVoicePolicy.usableVoice(Locale.UK, true))
        assertFalse(EnglishVoicePolicy.usableVoice(Locale.US, false))
    }

    @Test fun languageOnlyAvailabilityDoesNotSatisfyCountryRequirement() {
        assertFalse(EnglishVoicePolicy.hasCountryVoice(0, 1))
        assertTrue(EnglishVoicePolicy.hasCountryVoice(1, 1))
        assertTrue(EnglishVoicePolicy.hasCountryVoice(2, 1))
        assertFalse(EnglishVoicePolicy.hasCountryVoice(-1, 1))
    }

    @Test fun childRateAndTagAreFixed() {
        assertEquals("en-GB", EnglishVoicePolicy.LANGUAGE_TAG)
        assertTrue(EnglishVoicePolicy.CHILD_SPEECH_RATE in 0.75f..0.95f)
    }
}
