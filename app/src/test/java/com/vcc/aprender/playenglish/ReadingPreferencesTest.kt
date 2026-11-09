package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.audio.ReadingPreferences
import com.vcc.aprender.playenglish.audio.EnglishVoicePolicy
import org.junit.Assert.*
import org.junit.Test

class ReadingPreferencesTest {
    @Test fun defaultsAreSafeForChild() {
        val options = ReadingPreferences()
        assertTrue(options.audioEnabled)
        assertTrue(options.showInstructions)
        assertEquals(EnglishVoicePolicy.CHILD_SPEECH_RATE, options.speechRate, 0.0001f)
    }

    @Test fun readingRateCannotEscapeSafeBounds() {
        assertEquals(ReadingPreferences.MIN_RATE, ReadingPreferences(speechRate = -1f).normalized().speechRate, 0f)
        assertEquals(ReadingPreferences.MAX_RATE, ReadingPreferences(speechRate = 9f).normalized().speechRate, 0f)
        assertEquals(EnglishVoicePolicy.CHILD_SPEECH_RATE, ReadingPreferences(speechRate = Float.NaN).normalized().speechRate, 0f)
        assertEquals(EnglishVoicePolicy.CHILD_SPEECH_RATE, ReadingPreferences(speechRate = Float.POSITIVE_INFINITY).normalized().speechRate, 0f)
    }

    @Test fun togglesArePreservedWhenRateIsNormalized() {
        val options = ReadingPreferences(audioEnabled = false, speechRate = 5f, showInstructions = false).normalized()
        assertFalse(options.audioEnabled)
        assertFalse(options.showInstructions)
        assertEquals(ReadingPreferences.MAX_RATE, options.speechRate, 0f)
    }
}
