package com.vcc.aprender.playenglish.ui.vocabulary

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vcc.aprender.playenglish.data.AssetsQuizContentRepository
import com.vcc.aprender.playenglish.domain.usecase.LoadCategoryVocabularyUseCase

/** Application-context-only factory for offline vocabulary content. */
class VocabularyViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val repository = AssetsQuizContentRepository(context.applicationContext)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(VocabularyViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return VocabularyViewModel(LoadCategoryVocabularyUseCase(repository)) as T
    }
}
