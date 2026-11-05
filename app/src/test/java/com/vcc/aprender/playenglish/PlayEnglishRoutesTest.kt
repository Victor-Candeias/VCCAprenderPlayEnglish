package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.ui.navigation.PlayEnglishRoutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayEnglishRoutesTest {
    @Test
    fun initialRouteIsHomeAndDestinationsAreDistinct() {
        assertEquals("home", PlayEnglishRoutes.HOME)
        assertEquals(5, PlayEnglishRoutes.all.size)
        assertTrue(PlayEnglishRoutes.all.contains(PlayEnglishRoutes.HOME))
        assertTrue(PlayEnglishRoutes.all.contains(PlayEnglishRoutes.DISCOVER))
        assertTrue(PlayEnglishRoutes.all.contains(PlayEnglishRoutes.QUIZ))
        assertTrue(PlayEnglishRoutes.all.contains(PlayEnglishRoutes.PRONUNCIATION))
        assertTrue(PlayEnglishRoutes.all.contains(PlayEnglishRoutes.RESULTS))
    }
}
