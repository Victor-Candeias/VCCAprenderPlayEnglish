package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.domain.model.AgeRange
import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.domain.model.WordEntry
import com.vcc.aprender.playenglish.domain.usecase.SearchVocabularyUseCase
import org.junit.Assert.*
import org.junit.Test

class SearchVocabularyUseCaseTest {
    private fun entry(id: String, pt: String, en: String) = WordEntry(
        id = id, ptPT = pt, enGB = en, category = WordCategory.FOOD,
        ageRange = AgeRange(3, 8), imageRef = null, audioText = en
    )
    private val words = listOf(
        entry("apple", "Maçã", "Apple"),
        entry("bread", "Pão", "Bread"),
        entry("cake", "Bolo", "Cake")
    )

    @Test fun searchHandlesPortugueseAccentsAndCase() {
        assertEquals("apple", SearchVocabularyUseCase.execute(words, "MACA").single().id)
        assertEquals("bread", SearchVocabularyUseCase.execute(words, "pao").single().id)
        assertEquals("bread", SearchVocabularyUseCase.execute(words, " BrEaD ").single().id)
    }

    @Test fun searchingNeverChangesCatalogOrExistingOrder() {
        val before = words.toList()
        assertEquals(3, SearchVocabularyUseCase.execute(words, " ").size)
        assertTrue(SearchVocabularyUseCase.execute(words, "unknown").isEmpty())
        assertEquals(before, words)
        assertEquals(listOf("apple", "cake"),
            SearchVocabularyUseCase.execute(words, "a").map { it.id })
    }

    @Test fun randomSelectionAlwaysDiffersForMultiWordList() {
        for (old in 0 until 8) for (seedValue in 0 until 60) {
            val next = SearchVocabularyUseCase.randomIndex(8, old, seedValue)
            assertTrue(next in 0..7)
            assertNotEquals(old, next)
        }
        assertEquals(0, SearchVocabularyUseCase.randomIndex(1, 0, 999))
    }

    @Test(expected = IllegalArgumentException::class)
    fun randomRejectsEmptyList() { SearchVocabularyUseCase.randomIndex(0, 0, 1) }
}
