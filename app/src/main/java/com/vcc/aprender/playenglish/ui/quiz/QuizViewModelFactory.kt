package com.vcc.aprender.playenglish.ui.quiz

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vcc.aprender.playenglish.data.AssetsQuizContentRepository
import com.vcc.aprender.playenglish.domain.usecase.LoadQuizUseCase

/** Minimal composition root until a dependency injection framework is needed. */
class QuizViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val repository = AssetsQuizContentRepository(context.applicationContext)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(QuizViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return QuizViewModel(LoadQuizUseCase(repository)) as T
    }
}
