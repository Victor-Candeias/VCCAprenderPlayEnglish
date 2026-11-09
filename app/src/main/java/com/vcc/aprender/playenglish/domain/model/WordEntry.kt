package com.vcc.aprender.playenglish.domain.model

/**
 * Offline vocabulary identifiers are permanent content keys, not translated
 * display text. Changing an ID may invalidate saved learning progress.
 */
private val stableIdPattern = Regex("[a-z0-9]+([._-][a-z0-9]+)*")

internal fun requireStableId(id: String) {
    require(id.length in 1..80 && stableIdPattern.matches(id)) {
        "Content identifiers must be stable lowercase ASCII keys up to 80 chars."
    }
}

data class AgeRange(val min: Int, val max: Int) {
    init {
        require(min in 3..8 && max in 3..8 && min <= max) {
            "Supported age range is between 3 and 8 years."
        }
    }

    fun supports(age: Int): Boolean = age in min..max
}

enum class WordCategory(val id: String) {
    ANIMALS("animals"),
    COLORS("colors"),
    NUMBERS("numbers"),
    FOOD("food"),
    OBJECTS("objects"),
    PHRASES("phrases"),
    CLOTHING("clothing"),
    TRANSPORT("transport"),
    FAMILY("family"),
    BODY("body");

    companion object {
        fun fromId(id: String): WordCategory =
            values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("Unknown vocabulary category.")
    }
}

/**
 * A single offline word/expression in PT-PT and UK English.
 * imageRef may be null until PE-013 supplies actual local images.
 */
data class WordEntry(
    val id: String,
    val ptPT: String,
    val enGB: String,
    val category: WordCategory,
    val ageRange: AgeRange,
    val imageRef: String?,
    val audioText: String,
    val subcategory: String? = null
) {
    init {
        requireStableId(id)
        require(ptPT.isNotBlank() && enGB.isNotBlank() && audioText.isNotBlank()) {
            "PT-PT, en-GB and pronunciation text are required."
        }
        require(ptPT == ptPT.trim() && enGB == enGB.trim() &&
            audioText == audioText.trim()) { "Content must not have outer whitespace." }
        if (subcategory != null) {
            require(category == WordCategory.OBJECTS &&
                subcategory in setOf("home", "school")) {
                "Only supported object subcategories may be used."
            }
        }
        if (imageRef != null) {
            require(
                Regex("drawable/[a-z0-9_]+").matches(imageRef) ||
                (imageRef.startsWith("images/") && !imageRef.contains("..") &&
                    Regex("images/[a-z0-9_./-]+[.](png|webp|jpg)").matches(imageRef))
            ) { "imageRef must be a safe bundled resource path." }
        }
    }
}

/** The persisted quiz definition refers to WordEntry IDs only. */
data class QuizQuestionSpec(
    val id: String,
    val prompt: String,
    val optionIds: List<String>,
    val correctId: String
) {
    init {
        requireStableId(id)
        require(prompt.isNotBlank() && prompt == prompt.trim())
        require(optionIds.size == 3) { "A question must have exactly three options." }
        optionIds.forEach(::requireStableId)
        require(optionIds.distinct().size == 3) { "Three options must be distinct." }
        requireStableId(correctId)
        require(correctId in optionIds) { "Correct answer must be one of the options." }
    }
}

/**
 * Fail-closed catalog: duplicates, unresolved references or mismatched ages
 * are rejected before the quiz is exposed to the view model.
 */
data class OfflineVocabularyCatalog(
    val words: List<WordEntry>,
    val questions: List<QuizQuestionSpec>
) {
    init {
        require(words.size <= 10_000 && questions.size <= 10_000)
        require(words.map { it.id }.distinct().size == words.size) {
            "Duplicate word IDs."
        }
        require(questions.map { it.id }.distinct().size == questions.size) {
            "Duplicate question IDs."
        }
        val lookup = words.associateBy { it.id }
        questions.forEach { question ->
            val options = question.optionIds.map { id ->
                lookup[id] ?: throw IllegalArgumentException(
                    "Unknown word ID referenced in quiz question."
                )
            }
            require(options.maxOf { it.ageRange.min } <=
                options.minOf { it.ageRange.max }) {
                "A question must have a common supported age range."
            }
        }
    }
}
