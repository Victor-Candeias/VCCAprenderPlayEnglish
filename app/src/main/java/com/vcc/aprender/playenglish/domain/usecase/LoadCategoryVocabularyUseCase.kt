package com.vcc.aprender.playenglish.domain.usecase

import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.domain.model.WordEntry
import com.vcc.aprender.playenglish.domain.repository.VocabularyContentRepository

/** Domain-only grouping with stable category and word ordering. */
data class VocabularyCategoryGroup(
    val category: WordCategory,
    val words: List<WordEntry>
) {
    init {
        require(words.all { it.category == category })
    }

    val size: Int get() = words.size
}

class LoadCategoryVocabularyUseCase(private val content: VocabularyContentRepository) {
    fun execute(): List<VocabularyCategoryGroup> {
        val catalog = content.loadCatalog()
        return WordCategory.values().map { category ->
            VocabularyCategoryGroup(
                category = category,
                words = catalog.words.filter { it.category == category }
            )
        }
    }
}
