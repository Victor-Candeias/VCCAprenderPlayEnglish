package com.vcc.aprender.playenglish

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on Android device/emulator; CI compiles but does not run instrumented tests. */
@RunWith(AndroidJUnit4::class)
class PlayEnglishNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun rightAnswerShowsSmileAndNextQuestionClearsIt() {
        rule.onNodeWithTag("home-quiz").performClick()
        rule.onNodeWithTag("screen-quiz").assertExists()
        rule.onNodeWithTag("quiz-category-animals").performScrollTo().performClick()
        rule.onNodeWithTag("quiz-question-prompt").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-correct-smile").assertDoesNotExist()
        rule.onNodeWithTag("quiz-success-feedback").assertDoesNotExist()
        rule.onNodeWithTag("quiz-correct-count").performScrollTo().assertTextContains("0")
        rule.onNodeWithTag("quiz-next-preview").assertIsNotEnabled()
        rule.onNodeWithTag("quiz-option-b").performScrollTo().performTouchInput {
            down(center)
            advanceEventTime(100L)
            up()
        }
        rule.onNodeWithTag("quiz-correct-smile").assertDoesNotExist()
        rule.onNodeWithTag("quiz-waiting-state").performScrollTo().assertExists()
        // Accessibility activation: for animals.cat, B = Cat is the correct option.
        rule.onNodeWithTag("quiz-option-b").performClick()
        rule.onNodeWithTag("quiz-answer-registered").assertExists()
        rule.onNodeWithTag("quiz-success-feedback").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-correct-smile").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-correct-count").performScrollTo().assertTextContains("1")
        rule.onNodeWithTag("quiz-option-a").assertIsNotEnabled()
        rule.onNodeWithTag("quiz-option-b").assertIsNotEnabled()
        rule.onNodeWithTag("quiz-option-c").assertIsNotEnabled()
        rule.onNodeWithTag("quiz-next-preview").performScrollTo().assertIsEnabled()
        rule.onNodeWithTag("quiz-correct-smile").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-next-preview").performClick()
        rule.onNodeWithTag("quiz-question-prompt").assertExists()
        rule.onNodeWithTag("quiz-success-feedback").assertDoesNotExist()
        rule.onNodeWithTag("quiz-correct-smile").assertDoesNotExist()
        rule.onNodeWithTag("quiz-waiting-state").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-correct-count").performScrollTo().assertTextContains("1")
    }

    @Test fun beginnerExpressionsShowEmptyAndAdvancedExpressionsHaveQuestions() {
        rule.onNodeWithTag("home-quiz").performClick()
        rule.onNodeWithTag("quiz-category-phrases").performScrollTo().performClick()
        rule.onNodeWithTag("quiz-no-questions").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-conclude").assertDoesNotExist()
        rule.onNodeWithTag("quiz-level-6-8").performScrollTo().performClick()
        rule.onNodeWithTag("quiz-selection-count").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-next-preview").assertIsNotEnabled()
    }

    @Test fun colorSwatchAndThreeOptionsAreVisibleWithoutAnswerFeedback() {
        rule.onNodeWithTag("home-quiz").performClick()
        rule.onNodeWithTag("quiz-category-colors").performScrollTo().performClick()
        rule.onNodeWithTag("quiz-color-swatch").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-option-a").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-option-b").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-option-c").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-success-feedback").assertDoesNotExist()
        rule.onNodeWithTag("quiz-correct-smile").assertDoesNotExist()
    }

    @Test fun incorrectAnswerNeverShowsPositiveFeedback() {
        rule.onNodeWithTag("home-quiz").performClick()
        rule.onNodeWithTag("quiz-category-colors").performScrollTo().performClick()
        // First colors.red item: A = Green is wrong; B = Red is correct.
        rule.onNodeWithTag("quiz-option-a").performScrollTo().performClick()
        rule.onNodeWithTag("quiz-answer-registered").assertExists()
        rule.onNodeWithTag("quiz-correct-smile").assertDoesNotExist()
        rule.onNodeWithTag("quiz-success-feedback").assertDoesNotExist()
        rule.onNodeWithTag("quiz-correct-count").performScrollTo().assertTextContains("0")
        rule.onNodeWithTag("quiz-option-a").assertDoesNotExist()
        rule.onNodeWithTag("quiz-option-c").assertDoesNotExist()
        rule.onNodeWithTag("quiz-option-b").assertIsNotEnabled()
        rule.onNodeWithTag("quiz-error-feedback").performScrollTo()
            .assertTextContains("Resposta errada!")
        rule.onNodeWithTag("quiz-error-explanation").performScrollTo()
            .assertTextContains("Red")
        rule.onNodeWithTag("quiz-next-preview").performScrollTo().assertIsEnabled()
        rule.onNodeWithTag("quiz-next-preview").performClick()
        rule.onNodeWithTag("quiz-error-feedback").assertDoesNotExist()
        rule.onNodeWithTag("quiz-error-explanation").assertDoesNotExist()
        rule.onNodeWithTag("quiz-option-a").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-option-b").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-option-c").performScrollTo().assertExists()
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
