package com.vcc.aprender.playenglish.domain.repository

import com.vcc.aprender.playenglish.domain.model.QuizQuestion

/** Offline-only content port: unit tests can inject a memory-backed fake. */
interface QuizContentRepository {
    fun loadQuestions(): List<QuizQuestion>
}
