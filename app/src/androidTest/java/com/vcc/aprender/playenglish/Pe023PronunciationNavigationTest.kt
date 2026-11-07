package com.vcc.aprender.playenglish

import androidx.compose.ui.test.assertExists
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
class Pe023PronunciationNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun showsFilteredOfflineVocabularyAndNavigates() {
        rule.onNodeWithTag("home-discover").performClick()
        rule.onNodeWithTag("discover-category-animals").performScrollTo().performClick()
        rule.onNodeWithTag("discover-pronunciation").performScrollTo().performClick()
        rule.onNodeWithTag("pronunciation-portuguese").performScrollTo().assertExists()
        rule.onNodeWithTag("pronunciation-english").performScrollTo().assertExists()
        rule.onNodeWithTag("pronunciation-illustration").performScrollTo().assertExists()
        rule.onNodeWithTag("pronunciation-previous").performScrollTo().assertIsNotEnabled()
        rule.onNodeWithTag("pronunciation-next").performScrollTo().performClick()
        rule.onNodeWithTag("pronunciation-position").performScrollTo().assertExists()
    }

    @Test fun emptySelectionDoesNotExposeFakeSpeechOrScore() {
        rule.onNodeWithTag("home-discover").performClick()
        rule.onNodeWithTag("discover-pronunciation").performScrollTo().performClick()
        rule.onNodeWithTag("pronunciation-no-words").performScrollTo().assertExists()
        rule.onNodeWithTag("pronunciation-listen").performScrollTo().assertIsNotEnabled()
        rule.onNodeWithTag("pronunciation-repeat").performScrollTo().assertIsNotEnabled()
    }
}
