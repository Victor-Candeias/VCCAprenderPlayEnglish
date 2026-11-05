package com.vcc.aprender.playenglish

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vcc.aprender.playenglish.data.AssetsQuizContentRepository
import com.vcc.aprender.playenglish.domain.usecase.LoadQuizUseCase
import com.vcc.aprender.playenglish.domain.model.QuizPhase
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on Android device/emulator; GitHub CI compiles but does not execute. */
@RunWith(AndroidJUnit4::class)
class OfflineCatalogInstrumentedTest {
    @Test fun readsBundledCatalogWithoutNetwork() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val quiz = LoadQuizUseCase(AssetsQuizContentRepository(context)).execute()
        assertTrue(quiz.questions.isEmpty())
        assertEquals(QuizPhase.NOT_STARTED, quiz.phase)
        assertNull(quiz.score)
    }
}
