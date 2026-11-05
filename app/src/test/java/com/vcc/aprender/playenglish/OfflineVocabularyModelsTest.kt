package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.domain.model.AgeRange
import com.vcc.aprender.playenglish.domain.model.OfflineVocabularyCatalog
import com.vcc.aprender.playenglish.domain.model.QuizQuestionSpec
import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.domain.model.WordEntry
import org.junit.Assert.*
import org.junit.Test

class OfflineVocabularyModelsTest {
    private val cat = WordEntry(
        "animals.cat", "Gato", "Cat", WordCategory.ANIMALS,
        AgeRange(3, 8), null, "Cat"
    )
    private val dog = WordEntry(
        "animals.dog", "Cão", "Dog", WordCategory.ANIMALS,
        AgeRange(3, 8), null, "Dog"
    )
    private val bird = WordEntry(
        "animals.bird", "Pássaro", "Bird", WordCategory.ANIMALS,
        AgeRange(3, 8), null, "Bird"
    )
    private val question = QuizQuestionSpec(
        "quiz.animals.cat", "Como se diz «Gato» em inglês?",
        listOf("animals.dog", "animals.cat", "animals.bird"), "animals.cat"
    )

    @Test fun validUnicodeAndStableIds() {
        val catalog = OfflineVocabularyCatalog(listOf(cat, dog, bird), listOf(question))
        assertEquals(3, catalog.words.size)
        assertEquals(1, catalog.questions.size)
        assertEquals("Pássaro", catalog.words[2].ptPT)
        assertEquals(WordCategory.ANIMALS, WordCategory.fromId("animals"))
        assertTrue(cat.ageRange.supports(5))
        assertFalse(cat.ageRange.supports(9))
    }

    @Test fun rejectsEmptyAndInvalidTranslationOrIds() {
        assertThrows(IllegalArgumentException::class.java) {
            cat.copy(ptPT = "  ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            cat.copy(enGB = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            cat.copy(audioText = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            cat.copy(id = "http://example.org")
        }
        assertThrows(IllegalArgumentException::class.java) {
            cat.copy(imageRef = "https://server/cat.png")
        }
    }

    @Test fun rejectsBadAgeRangeOrUnknownCategory() {
        assertThrows(IllegalArgumentException::class.java) { AgeRange(2, 8) }
        assertThrows(IllegalArgumentException::class.java) { AgeRange(8, 3) }
        assertThrows(IllegalArgumentException::class.java) {
            WordCategory.fromId("aliens")
        }
    }

    @Test fun questionRequiresExactlyThreeDistinctKnownOptions() {
        assertThrows(IllegalArgumentException::class.java) {
            question.copy(optionIds = listOf("animals.cat", "animals.dog"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            question.copy(optionIds = listOf("animals.cat", "animals.cat", "animals.bird"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            question.copy(correctId = "animals.fox")
        }
        assertThrows(IllegalArgumentException::class.java) {
            OfflineVocabularyCatalog(
                listOf(cat, dog, bird),
                listOf(question.copy(optionIds = listOf(
                    "animals.cat", "animals.dog", "animals.fox"
                )))
            )
        }
    }

    @Test fun catalogRejectsDuplicateIdentifiers() {
        assertThrows(IllegalArgumentException::class.java) {
            OfflineVocabularyCatalog(listOf(cat, cat), listOf(question))
        }
        assertThrows(IllegalArgumentException::class.java) {
            OfflineVocabularyCatalog(listOf(cat, dog, bird), listOf(question, question))
        }
    }

    @Test fun questionOptionsNeedOverlappingAges() {
        val child = cat.copy(ageRange = AgeRange(3, 3))
        val older = dog.copy(ageRange = AgeRange(8, 8))
        assertThrows(IllegalArgumentException::class.java) {
            OfflineVocabularyCatalog(listOf(child, older, bird), listOf(question))
        }
    }
}
