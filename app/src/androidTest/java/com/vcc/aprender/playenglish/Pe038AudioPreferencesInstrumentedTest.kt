package com.vcc.aprender.playenglish

import android.content.Context
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Pe038AudioPreferencesInstrumentedTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun disablingAudioBlocksPlaybackAndPreferencesSurviveRecreation() {
        // Reset private preferences and recreate the Activity before displaying the UI.
        rule.activity.getSharedPreferences("pe038_reading_options", Context.MODE_PRIVATE)
            .edit().clear().commit()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("home-discover").performClick()
        rule.onNodeWithTag("discover-category-animals").performScrollTo().performClick()
        rule.onNodeWithTag("discover-pronunciation").performScrollTo().performClick()
        rule.onNodeWithTag("reading-audio-toggle").performScrollTo().performClick()
        rule.onNodeWithTag("pronunciation-listen").performScrollTo().assertIsNotEnabled()
        rule.activityRule.scenario.recreate()
        // Navigation can be restored or reset by Android, so persistence is
        // checked directly against the local application preference store.
        val saved = rule.activity.getSharedPreferences("pe038_reading_options", Context.MODE_PRIVATE)
        rule.waitForIdle()
        org.junit.Assert.assertFalse(saved.getBoolean("audio_enabled", true))
    }

    @Test fun readingControlsAreVisible() {
        rule.onNodeWithTag("home-discover").performClick()
        rule.onNodeWithTag("discover-pronunciation").performScrollTo().performClick()
        rule.onNodeWithTag("reading-preferences").performScrollTo().assertExists()
        rule.onNodeWithTag("reading-speed-slider").performScrollTo().assertExists()
        rule.onNodeWithTag("reading-instructions-toggle").performScrollTo().assertExists()
    }
}
