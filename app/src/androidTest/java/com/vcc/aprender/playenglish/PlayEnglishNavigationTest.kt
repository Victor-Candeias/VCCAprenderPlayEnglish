package com.vcc.aprender.playenglish

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on Android device/emulator; CI compiles but does not run instrumented tests. */
@RunWith(AndroidJUnit4::class)
class PlayEnglishNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun quizRequiresARealCategorySelectionBeforePreviewResults() {
        rule.onNodeWithTag("screen-home").assertExists()
        rule.onNodeWithTag("home-discover").assertExists()
        rule.onNodeWithTag("home-quiz").assertExists()
        rule.onNodeWithTag("home-quiz").performClick()
        rule.onNodeWithTag("screen-quiz").assertExists()
        rule.onNodeWithTag("quiz-level-3-5").assertExists()
        rule.onNodeWithTag("quiz-preview-results").assertIsNotEnabled()
        rule.onNodeWithTag("quiz-category-animals").performScrollTo().performClick()
        rule.onNodeWithTag("quiz-selection-count").assertExists()
        rule.onNodeWithTag("quiz-preview-results").performScrollTo().assertIsEnabled()
        rule.onNodeWithTag("quiz-preview-results").performClick()
        rule.onNodeWithTag("screen-results").assertExists()
        rule.onNodeWithTag("results-home").performClick()
        rule.onNodeWithTag("screen-home").assertExists()
    }

    @Test fun beginnerExpressionsShowEmptyAndAdvancedExpressionsHaveQuestions() {
        rule.onNodeWithTag("home-quiz").performClick()
        rule.onNodeWithTag("quiz-category-phrases").performScrollTo().performClick()
        rule.onNodeWithTag("quiz-no-questions").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-preview-results").assertIsNotEnabled()
        rule.onNodeWithTag("quiz-level-6-8").performScrollTo().performClick()
        rule.onNodeWithTag("quiz-selection-count").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-preview-results").assertIsEnabled()
    }

    @Test fun discoverFiltersWordsAndPreservesSelectionAcrossScreens() {
        rule.onNodeWithTag("home-discover").performClick()
        rule.onNodeWithTag("screen-discover").assertExists()
        rule.onNodeWithTag("discover-category-objects").performScrollTo().performClick()
        rule.onNodeWithTag("discover-no-words").performScrollTo().assertExists()
        rule.onNodeWithTag("discover-level-6-8").performScrollTo().performClick()
        rule.onNodeWithTag("discover-word-objects.book").performScrollTo().assertExists()
        rule.onNodeWithTag("discover-category-objects").performScrollTo().performClick()
        rule.onNodeWithTag("discover-pronunciation").performScrollTo().performClick()
        rule.onNodeWithTag("screen-pronunciation").assertExists()
        rule.onNodeWithTag("nav-back").performClick()
        rule.onNodeWithTag("screen-discover").assertExists()
        rule.onNodeWithTag("nav-back").performClick()
        rule.onNodeWithTag("screen-home").assertExists()
        rule.onNodeWithTag("home-quiz").performClick()
        rule.onNodeWithTag("quiz-level-6-8").assertExists()
    }
}
