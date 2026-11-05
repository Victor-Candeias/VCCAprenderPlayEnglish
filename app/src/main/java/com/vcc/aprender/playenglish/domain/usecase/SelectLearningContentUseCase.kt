package com.vcc.aprender.playenglish.domain.usecase

import com.vcc.aprender.playenglish.domain.model.AgeRange
import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.domain.model.WordEntry
import com.vcc.aprender.playenglish.domain.repository.VocabularyContentRepository

/**
 * Locally chosen *learning level*, not the child's actual age.
 * A word must be suitable for the ENTIRE selected band.
 */
enum class LearningLevel(val minAge: Int, val maxAge: Int) {
    AGES_3_TO_5(3, 5),
    AGES_6_TO_8(6, 8);

    fun includes(range: AgeRange): Boolean =
        range.min <= minAge && range.max >= maxAge
}

/** Domain data only: correct answers are not passed to the selection UI. */
data class LearningCategoryContent(
    val category: WordCategory,
    val words: List<WordEntry>,
    val questionIds: List<String>
) {
    init {
        require(words.all { it.category == category })
        require(questionIds.distinct().size == questionIds.size)
    }

    val wordCount: Int get() = words.size
    val questionCount: Int get() = questionIds.size
}

/**
 * Produces counts and filterable IDs from the real, validated offline catalog.
 * A question qualifies only when its answer and ALL three options belong to
 * the category and cover the selected complete age band.
 */
class SelectLearningContentUseCase(private val source: VocabularyContentRepository) {
    fun execute(level: LearningLevel): List<LearningCategoryContent> {
        val catalog = source.loadCatalog()
        val wordsById = catalog.words.associateBy { it.id }
        val eligibleIds = catalog.words.asSequence()
            .filter { level.includes(it.ageRange) }
            .map { it.id }
            .toSet()

        return WordCategory.values().map { category ->
            val words = catalog.words.filter {
                it.category == category && it.id in eligibleIds
            }
            val ids = catalog.questions.filter { question ->
                question.correctId in eligibleIds &&
                    wordsById[question.correctId]?.category == category &&
                    question.optionIds.all { id ->
                        id in eligibleIds && wordsById[id]?.category == category
                    }
            }.map { it.id }
            LearningCategoryContent(category, words, ids)
        }
    }
}
