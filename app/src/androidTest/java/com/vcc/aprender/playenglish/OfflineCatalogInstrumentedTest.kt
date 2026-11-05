package com.vcc.aprender.playenglish

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vcc.aprender.playenglish.data.AssetsQuizContentRepository
import com.vcc.aprender.playenglish.domain.usecase.LoadQuizUseCase
import com.vcc.aprender.playenglish.domain.model.QuizPhase
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Executes on Android device/emulator; GitHub CI compiles these tests only. */
@RunWith(AndroidJUnit4::class)
class OfflineCatalogInstrumentedTest {
    @Test fun loadsBundledPtPtEnGbVocabularyAndQuestionsWithoutNetwork() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AssetsQuizContentRepository(context)
        val catalog = repository.loadCatalog()
        assertEquals(72, catalog.words.size)
        assertEquals(48, catalog.questions.size)
        assertEquals("Pássaro",
            catalog.words.first { it.id == "animals.bird" }.ptPT)
        assertTrue(catalog.words.all { it.imageRef == null })
        assertTrue(catalog.words.groupBy { it.category }.values.all { it.size == 12 })
        val quiz = LoadQuizUseCase(repository).execute()
        assertEquals(QuizPhase.ACTIVE, quiz.phase)
        assertEquals(48, quiz.questionCount)
        assertNull(quiz.score)
        val resolved = repository.loadQuestions()
        assertEquals(48, resolved.size)
        assertEquals(3, resolved.first().choices.size)
        assertEquals(1, resolved.first().choices.count {
            it.id == resolved.first().correctChoiceId
        })
    }
}
