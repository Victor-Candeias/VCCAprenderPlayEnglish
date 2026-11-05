package com.vcc.aprender.playenglish

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vcc.aprender.playenglish.data.AssetsQuizContentRepository
import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.domain.usecase.LearningLevel
import com.vcc.aprender.playenglish.domain.usecase.SelectLearningContentUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LearningSelectionInstrumentedTest {
    @Test fun bundledCatalogOffersRealPerLevelCountsOffline() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val filter = SelectLearningContentUseCase(AssetsQuizContentRepository(context))
        val beginners = filter.execute(LearningLevel.AGES_3_TO_5)
        val advanced = filter.execute(LearningLevel.AGES_6_TO_8)
        assertEquals(6, beginners.size)
        assertEquals(6, advanced.size)
        assertEquals(48, beginners.sumOf { it.wordCount })
        assertEquals(32, beginners.sumOf { it.questionCount })
        assertEquals(72, advanced.sumOf { it.wordCount })
        assertEquals(48, advanced.sumOf { it.questionCount })
        assertEquals(0, beginners.first { it.category == WordCategory.OBJECTS }.questionCount)
        assertEquals(0, beginners.first { it.category == WordCategory.PHRASES }.wordCount)
        assertEquals(8, advanced.first { it.category == WordCategory.PHRASES }.questionCount)
    }
}
