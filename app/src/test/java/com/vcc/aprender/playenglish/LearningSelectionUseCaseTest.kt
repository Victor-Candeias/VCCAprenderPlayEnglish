package com.vcc.aprender.playenglish

import com.vcc.aprender.playenglish.domain.model.AgeRange
import com.vcc.aprender.playenglish.domain.model.OfflineVocabularyCatalog
import com.vcc.aprender.playenglish.domain.model.QuizQuestionSpec
import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.domain.model.WordEntry
import com.vcc.aprender.playenglish.domain.repository.VocabularyContentRepository
import com.vcc.aprender.playenglish.domain.usecase.LearningLevel
import com.vcc.aprender.playenglish.domain.usecase.SelectLearningContentUseCase
import com.vcc.aprender.playenglish.ui.learning.LearningSelectionViewModel
import org.junit.Assert.*
import org.junit.Test

class LearningSelectionUseCaseTest {
    private fun word(
        id: String, category: WordCategory, minAge: Int = 3
    ) = WordEntry(
        id = id, ptPT = "Texto", enGB = "Word",
        category = category, ageRange = AgeRange(minAge, 8),
        imageRef = null, audioText = "Word"
    )

    private fun source(data: OfflineVocabularyCatalog): VocabularyContentRepository =
        object : VocabularyContentRepository {
            override fun loadCatalog() = data
        }

    private val sample = OfflineVocabularyCatalog(
        words = listOf(
            word("animals.cat", WordCategory.ANIMALS),
            word("animals.dog", WordCategory.ANIMALS),
            word("animals.bird", WordCategory.ANIMALS),
            word("objects.book", WordCategory.OBJECTS, 4),
            word("objects.chair", WordCategory.OBJECTS, 4),
            word("objects.table", WordCategory.OBJECTS, 4),
            word("phrases.hello", WordCategory.PHRASES, 5),
            word("phrases.goodbye", WordCategory.PHRASES, 5),
            word("phrases.please", WordCategory.PHRASES, 5)
        ),
        questions = listOf(
            QuizQuestionSpec(
                "quiz.animals.cat", "Como se diz «Gato»?",
                listOf("animals.cat", "animals.dog", "animals.bird"),
                "animals.cat"
            ),
            QuizQuestionSpec(
                "quiz.objects.book", "Como se diz «Livro»?",
                listOf("objects.book", "objects.chair", "objects.table"),
                "objects.book"
            ),
            QuizQuestionSpec(
                "quiz.phrases.hello", "Como se diz «Olá»?",
                listOf("phrases.hello", "phrases.goodbye", "phrases.please"),
                "phrases.hello"
            )
        )
    )

    @Test fun threeToFiveKeepsOnlyEntireBandSafeWordsAndQuestions() {
        val content = SelectLearningContentUseCase(source(sample))
            .execute(LearningLevel.AGES_3_TO_5)
        assertEquals(WordCategory.values().size, content.size)
        assertEquals(3, content.first { it.category == WordCategory.ANIMALS }.wordCount)
        assertEquals(listOf("quiz.animals.cat"),
            content.first { it.category == WordCategory.ANIMALS }.questionIds)
        assertEquals(0, content.first { it.category == WordCategory.OBJECTS }.wordCount)
        assertEquals(0, content.first { it.category == WordCategory.PHRASES }.questionCount)
    }

    @Test fun sixToEightUnlocksHigherMinAgeWordsAndQuestions() {
        val content = SelectLearningContentUseCase(source(sample))
            .execute(LearningLevel.AGES_6_TO_8)
        assertEquals(3, content.first { it.category == WordCategory.OBJECTS }.wordCount)
        assertEquals(1, content.first { it.category == WordCategory.OBJECTS }.questionCount)
        assertEquals(1, content.first { it.category == WordCategory.PHRASES }.questionCount)
    }

    @Test fun refusesQuestionWhoseDistractorIsNotAgeSuitable() {
        val mixed = OfflineVocabularyCatalog(
            words = listOf(
                word("animals.cat", WordCategory.ANIMALS, 3),
                word("animals.dog", WordCategory.ANIMALS, 3),
                word("animals.bird", WordCategory.ANIMALS, 5)
            ),
            questions = listOf(
                QuizQuestionSpec("quiz.animals.cat", "Pergunta",
                    listOf("animals.cat", "animals.dog", "animals.bird"),
                    "animals.cat")
            )
        )
        val young = SelectLearningContentUseCase(source(mixed))
            .execute(LearningLevel.AGES_3_TO_5)
        assertEquals(2, young.first { it.category == WordCategory.ANIMALS }.wordCount)
        assertEquals(0, young.first { it.category == WordCategory.ANIMALS }.questionCount)
    }

    @Test fun viewModelTracksSelectionLocallyAndResetsOnInvalidCatalog() {
        val vm = LearningSelectionViewModel(SelectLearningContentUseCase(source(sample)))
        assertEquals(LearningLevel.AGES_3_TO_5, vm.uiState.value.level)
        assertNull(vm.uiState.value.selectedCategory)
        assertTrue(vm.uiState.value.selectedQuestionIds.isEmpty())
        vm.chooseCategory(WordCategory.PHRASES)
        assertTrue(vm.uiState.value.hasSelectedCategory)
        assertEquals(0, vm.uiState.value.selectedContent!!.questionCount)
        vm.chooseLevel(LearningLevel.AGES_6_TO_8)
        assertEquals(listOf("quiz.phrases.hello"), vm.uiState.value.selectedQuestionIds)
        vm.chooseCategory(WordCategory.PHRASES)
        assertFalse(vm.uiState.value.hasSelectedCategory)

        val failing = object : VocabularyContentRepository {
            override fun loadCatalog(): OfflineVocabularyCatalog = error("broken")
        }
        val brokenVm = LearningSelectionViewModel(SelectLearningContentUseCase(failing))
        assertTrue(brokenVm.uiState.value.catalogError)
        assertTrue(brokenVm.uiState.value.categories.isEmpty())
        assertTrue(brokenVm.uiState.value.selectedQuestionIds.isEmpty())
        brokenVm.chooseCategory(WordCategory.ANIMALS)
        assertNull(brokenVm.uiState.value.selectedCategory)
    }
}
