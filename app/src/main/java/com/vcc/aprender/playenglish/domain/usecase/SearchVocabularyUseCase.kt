package com.vcc.aprender.playenglish.domain.usecase

import com.vcc.aprender.playenglish.domain.model.WordEntry
import java.text.Normalizer
import java.util.Locale

/**
 * PE-024: immutable, diacritic-insensitive offline search. Never modifies the
 * source catalog or changes the user's existing age/category selection.
 */
object SearchVocabularyUseCase {
    fun normalize(value: String): String {
        val decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
    }

    fun execute(words: List<WordEntry>, query: String): List<WordEntry> {
        val search = normalize(query)
        if (search.isEmpty()) return words.toList()
        return words.filter { word ->
            normalize(word.ptPT).contains(search) ||
                normalize(word.enGB).contains(search)
        }
    }

    /** A different word is chosen when there is more than one option. */
    fun randomIndex(count: Int, currentIndex: Int, value: Int): Int {
        require(count > 0)
        require(value >= 0)
        if (count == 1) return 0
        val safeCurrent = currentIndex.coerceIn(0, count - 1)
        val other = value % (count - 1)
        return if (other >= safeCurrent) other + 1 else other
    }
}
