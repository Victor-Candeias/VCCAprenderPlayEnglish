package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.domain.model.*
import com.vcc.aprender.playenglish.domain.repository.VocabularyContentRepository
import com.vcc.aprender.playenglish.domain.usecase.LoadCategoryVocabularyUseCase
import com.vcc.aprender.playenglish.ui.vocabulary.VocabularyViewModel
import org.junit.Assert.*
import org.junit.Test

class CategoryVocabularyUseCaseTest {
    private fun word(id: String, category: WordCategory): WordEntry = WordEntry(
        id = id, ptPT = "Palavra", enGB = "Word",
        category = category, ageRange = AgeRange(3, 8),
        imageRef = null, audioText = "Word"
    )

    private fun repository(catalog: OfflineVocabularyCatalog): VocabularyContentRepository =
        object : VocabularyContentRepository {
            override fun loadCatalog(): OfflineVocabularyCatalog = catalog
        }

    @Test fun groupsWordsWithoutChangingIdsOrExistingInput() {
        val animal = word("animals.cat", WordCategory.ANIMALS)
        val color = word("colors.blue", WordCategory.COLORS)
        val catalog = OfflineVocabularyCatalog(listOf(animal, color), emptyList())
        val inputSnapshot = catalog.words.toList()
        val groups = LoadCategoryVocabularyUseCase(repository(catalog)).execute()
        assertEquals(WordCategory.values().size, groups.size)
        assertEquals(listOf(animal), groups.first { it.category == WordCategory.ANIMALS }.words)
        assertEquals(listOf(color), groups.first { it.category == WordCategory.COLORS }.words)
        assertEquals(0, groups.first { it.category == WordCategory.FOOD }.size)
        assertEquals(inputSnapshot, catalog.words)
    }

    @Test fun viewModelExposesGroupsAndNoHostIdentity() {
        val catalog = OfflineVocabularyCatalog(
            listOf(word("animals.cat", WordCategory.ANIMALS)),
            emptyList()
        )
        val vm = VocabularyViewModel(LoadCategoryVocabularyUseCase(repository(catalog)))
        assertEquals(1, vm.uiState.value.totalWords)
        assertFalse(vm.uiState.value.catalogError)
        assertEquals(WordCategory.values().size, vm.uiState.value.groups.size)
    }

    @Test fun invalidSourceShowsSafeEmptyState() {
        val broken = object : VocabularyContentRepository {
            override fun loadCatalog(): OfflineVocabularyCatalog = error("Bad catalog")
        }
        val vm = VocabularyViewModel(LoadCategoryVocabularyUseCase(broken))
        assertTrue(vm.uiState.value.catalogError)
        assertEquals(0, vm.uiState.value.totalWords)
        assertTrue(vm.uiState.value.groups.isEmpty())
    }
}
