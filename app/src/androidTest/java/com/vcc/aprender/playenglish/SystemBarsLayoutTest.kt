package com.vcc.aprender.playenglish

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Checks real window geometry, including the device's cutout and navigation mode. */
@RunWith(AndroidJUnit4::class)
class SystemBarsLayoutTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun scrollingAndNavigationKeepContentInsideSystemBars() {
        checkNavigationAndScrolling()
    }

    @Test fun landscapeKeepsControlsClearOfTheCutoutAndNavigationButtons() {
        val originalOrientation = rule.activity.requestedOrientation
        try {
            rule.activityRule.scenario.onActivity {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            rule.waitUntil(timeoutMillis = 10_000) {
                rule.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            }
            checkNavigationAndScrolling()
        } finally {
            rule.activityRule.scenario.onActivity { it.requestedOrientation = originalOrientation }
        }
    }

    private fun checkNavigationAndScrolling() {
        rule.runOnIdle {
            val window = rule.activity.window
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            assertTrue("Dark status icons must be readable on the light background",
                controller.isAppearanceLightStatusBars)
            if (Build.VERSION.SDK_INT >= 27) {
                assertTrue("Dark navigation icons must be readable on the light background",
                    controller.isAppearanceLightNavigationBars)
            }
        }
        assertSafeViewport()
        scrollToSafeControl("home-discover")
        rule.onNodeWithTag("home-discover").performClick()
        assertSafeViewport()
        scrollToSafeControl("nav-back")
        rule.onNodeWithTag("discover-category-animals").performScrollTo().performClick()
        scrollToSafeControl("discover-pronunciation")
        rule.onNodeWithTag("discover-pronunciation").performClick()
        assertSafeViewport()
        scrollToSafeControl("pronunciation-next")
        scrollToSafeControl("nav-back")
        rule.onNodeWithTag("nav-back").performClick()
        scrollToSafeControl("nav-back")
        rule.onNodeWithTag("nav-back").performClick()
        scrollToSafeControl("home-quiz")
        rule.onNodeWithTag("home-quiz").performClick()
        assertSafeViewport()
        rule.onNodeWithTag("quiz-category-colors").performScrollTo().performClick()
        scrollToSafeControl("quiz-next-preview")
        scrollToSafeControl("nav-back")
        repeat(25) {
            rule.onNodeWithTag("quiz-option-a").performScrollTo()
                .performSemanticsAction(SemanticsActions.OnClick) { click -> click() }
            if (rule.onAllNodesWithTag("quiz-conclude").fetchSemanticsNodes().isNotEmpty()) {
                scrollToSafeControl("quiz-conclude")
                rule.onNodeWithTag("quiz-conclude").performClick()
                scrollToSafeControl("results-home")
                scrollToSafeControl("nav-back")
                return
            }
            scrollToSafeControl("quiz-next-preview")
            rule.onNodeWithTag("quiz-next-preview").performClick()
        }
        throw AssertionError("The colors quiz must reach the results screen")
    }

    private fun scrollToSafeControl(tag: String) {
        val control = rule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
        assertSafeViewport()
        val bounds = control.fetchSemanticsNode().boundsInWindow
        val viewport = rule.onNodeWithTag("screen-safe-viewport")
            .fetchSemanticsNode().boundsInWindow
        assertTrue("$tag must remain above navigation buttons: $bounds / $viewport",
            bounds.bottom <= viewport.bottom + 1f)
        assertTrue("$tag must remain below the status bar: $bounds / $viewport",
            bounds.top >= viewport.top - 1f)
    }

    private fun assertSafeViewport() {
        rule.waitForIdle()
        val bounds = rule.onNodeWithTag("screen-safe-viewport")
            .fetchSemanticsNode().boundsInWindow
        rule.runOnIdle {
            val decor = rule.activity.window.decorView
            val insets = requireNotNull(ViewCompat.getRootWindowInsets(decor)).getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            assertTrue("Content overlaps the status bar/cutout: $bounds / $insets",
                bounds.top >= insets.top - 1f)
            assertTrue("Content overlaps navigation buttons: $bounds / $insets",
                bounds.bottom <= decor.height - insets.bottom + 1f)
            assertTrue("Content overlaps the left cutout: $bounds / $insets",
                bounds.left >= insets.left - 1f)
            assertTrue("Content overlaps the right cutout: $bounds / $insets",
                bounds.right <= decor.width - insets.right + 1f)
        }
    }
}
