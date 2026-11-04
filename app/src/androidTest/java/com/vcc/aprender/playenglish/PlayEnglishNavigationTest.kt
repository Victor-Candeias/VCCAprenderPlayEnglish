package com.vcc.aprender.playenglish

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayEnglishNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeNavigatesThroughQuizResultsAndBackHome() {
        rule.onNodeWithTag("screen-home").assertExists()
        rule.onNodeWithTag("home-quiz").performClick()
        rule.onNodeWithTag("screen-quiz").assertExists()
        rule.onNodeWithTag("quiz-preview-results").performClick()
        rule.onNodeWithTag("screen-results").assertExists()
        rule.onNodeWithTag("results-home").performClick()
        rule.onNodeWithTag("screen-home").assertExists()
    }

    @Test
    fun homeNavigatesToPronunciationAndBack() {
        rule.onNodeWithTag("home-pronunciation").performClick()
        rule.onNodeWithTag("screen-pronunciation").assertExists()
        rule.onNodeWithTag("pronunciation-audio-disabled").assertIsNotEnabled()
        rule.onNodeWithTag("nav-back").performClick()
        rule.onNodeWithTag("screen-home").assertExists()
    }
}
