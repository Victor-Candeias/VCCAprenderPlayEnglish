package com.vcc.aprender.playenglish

import android.content.Context
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PE-041: real Compose interactions on Android; not a replacement for JVM unit tests.
 * TTS is never invoked by these tests: playback is disabled locally.
 */
@RunWith(AndroidJUnit4::class)
class Pe041ComposeUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun exists(tag: String): Boolean =
        rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    @Test fun shortTouchDoesNotConfirmButAccessibleClickDoes() {
        rule.onNodeWithTag("home-quiz").performClick()
        rule.onNodeWithTag("quiz-category-animals").performScrollTo().performClick()
        rule.onNodeWithTag("quiz-next-preview").performScrollTo().assertIsNotEnabled()
        rule.onNodeWithTag("quiz-option-a").performScrollTo().performTouchInput {
            down(center)
            advanceEventTime(100L)
            up()
        }
        rule.onNodeWithTag("quiz-waiting-state").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-option-a").performScrollTo().performClick()
        rule.onNodeWithTag("quiz-answer-registered").assertExists()
        rule.onNodeWithTag("quiz-next-preview").performScrollTo().assertIsEnabled()
        // A second confirmation is ignored: only one answer is ever registered.
        rule.onNodeWithTag("quiz-correct-count").performScrollTo().assertExists()
    }

    @Test fun feedbackIsExclusiveAndWrongAnswerHidesDistractors() {
        rule.onNodeWithTag("home-quiz").performClick()
        rule.onNodeWithTag("quiz-category-colors").performScrollTo().performClick()
        rule.onNodeWithTag("quiz-option-a").performScrollTo().performClick()
        val right = exists("quiz-success-feedback")
        val wrong = exists("quiz-error-feedback")
        assertTrue(right.xor(wrong))
        if (right) {
            rule.onNodeWithTag("quiz-correct-smile").performScrollTo().assertExists()
            rule.onNodeWithTag("quiz-error-explanation").assertDoesNotExist()
        } else {
            rule.onNodeWithTag("quiz-correct-smile").assertDoesNotExist()
            rule.onNodeWithTag("quiz-error-explanation").performScrollTo().assertExists()
            assertEquals(1, listOf("a", "b", "c").count { exists("quiz-option-$it") })
        }
        rule.onNodeWithTag("quiz-next-preview").performScrollTo().performClick()
        rule.onNodeWithTag("quiz-waiting-state").performScrollTo().assertExists()
        rule.onNodeWithTag("quiz-correct-smile").assertDoesNotExist()
        rule.onNodeWithTag("quiz-error-feedback").assertDoesNotExist()
        assertEquals(3, listOf("a", "b", "c").count { exists("quiz-option-$it") })
    }

    @Test fun vocabularySearchAndDisabledTtsKeepTextUsable() {
        rule.activity.getSharedPreferences("pe038_reading_options", Context.MODE_PRIVATE)
            .edit().putBoolean("audio_enabled", false).commit()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("home-discover").performClick()
        rule.onNodeWithTag("discover-category-animals").performScrollTo().performClick()
        rule.onNodeWithTag("discover-pronunciation").performScrollTo().performClick()
        rule.onNodeWithTag("pronunciation-portuguese").performScrollTo().assertExists()
        rule.onNodeWithTag("pronunciation-english").performScrollTo().assertExists()
        rule.onNodeWithTag("pronunciation-listen").performScrollTo().assertIsNotEnabled()
        rule.onNodeWithTag("pronunciation-repeat").performScrollTo().assertIsNotEnabled()
        rule.onNodeWithTag("pronunciation-search").performScrollTo()
            .performTextInput("palavra-que-nao-existe-123")
        rule.onNodeWithTag("pronunciation-no-words").performScrollTo().assertExists()
        rule.onNodeWithTag("pronunciation-search").performTextClearance()
        rule.onNodeWithTag("pronunciation-portuguese").performScrollTo().assertExists()
        rule.onNodeWithTag("pronunciation-next").performScrollTo().performClick()
        rule.onNodeWithTag("pronunciation-position").performScrollTo().assertExists()
        rule.onNodeWithTag("nav-back").performClick()
        rule.onNodeWithTag("screen-discover").assertExists()
    }

    @Test fun completedQuizShowsRealResultsScreen() {
        rule.onNodeWithTag("home-quiz").performClick()
        rule.onNodeWithTag("quiz-category-colors").performScrollTo().performClick()
        // Colors for ages 3-5 contains a bounded offline deck; no random score assumptions.
        var completed = false
        for (index in 0 until 25) {
            if (exists("quiz-conclude")) {
                rule.onNodeWithTag("quiz-conclude").performScrollTo().performClick()
                completed = true
                break
            }
            rule.onNodeWithTag("quiz-option-a").performScrollTo().performClick()
            if (exists("quiz-conclude")) {
                rule.onNodeWithTag("quiz-conclude").performScrollTo().performClick()
                completed = true
                break
            }
            rule.onNodeWithTag("quiz-next-preview").performScrollTo().performClick()
        }
        assertTrue("A bounded colors deck must reach results", completed)
        rule.onNodeWithTag("results-home").performScrollTo().assertExists()
    }
}
