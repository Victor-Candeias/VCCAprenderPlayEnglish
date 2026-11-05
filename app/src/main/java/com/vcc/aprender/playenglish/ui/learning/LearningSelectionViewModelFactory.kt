package com.vcc.aprender.playenglish.ui.learning

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vcc.aprender.playenglish.data.AssetsQuizContentRepository
import com.vcc.aprender.playenglish.domain.usecase.SelectLearningContentUseCase

class LearningSelectionViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val repository = AssetsQuizContentRepository(context.applicationContext)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(LearningSelectionViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return LearningSelectionViewModel(SelectLearningContentUseCase(repository)) as T
    }
}
