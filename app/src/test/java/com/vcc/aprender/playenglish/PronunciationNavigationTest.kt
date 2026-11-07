package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.ui.pronunciation.PronunciationNavigation
import org.junit.Assert.*
import org.junit.Test

class PronunciationNavigationTest {
    @Test fun emptyOrSingleDoesNotAdvance() {
        assertEquals(0, PronunciationNavigation.next(0, 0))
        assertEquals(0, PronunciationNavigation.previous(0, 0))
        assertEquals(0, PronunciationNavigation.next(0, 1))
        assertEquals(0, PronunciationNavigation.previous(0, 1))
    }
    @Test fun nextPreviousStayWithinEdges() {
        assertEquals(1, PronunciationNavigation.next(0, 3))
        assertEquals(2, PronunciationNavigation.next(1, 3))
        assertEquals(2, PronunciationNavigation.next(2, 3))
        assertEquals(1, PronunciationNavigation.previous(2, 3))
        assertEquals(0, PronunciationNavigation.previous(0, 3))
    }
    @Test fun staleIndexIsClamped() {
        assertEquals(1, PronunciationNavigation.next(999, 2))
        assertEquals(0, PronunciationNavigation.previous(-99, 2))
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeCount() { PronunciationNavigation.next(0, -1) }
}
