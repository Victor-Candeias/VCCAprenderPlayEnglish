package com.vcc.aprender.playenglish.ui.vocabulary

import androidx.lifecycle.ViewModel
import com.vcc.aprender.playenglish.domain.usecase.LoadCategoryVocabularyUseCase
import com.vcc.aprender.playenglish.domain.usecase.VocabularyCategoryGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Read-only projection for the Discover screen; no host identifiers exposed. */
data class VocabularyUiState(
    val groups: List<VocabularyCategoryGroup> = emptyList(),
    val catalogError: Boolean = false
) {
    val totalWords: Int get() = groups.sumOf { it.size }
}

class VocabularyViewModel(
    private val loadVocabulary: LoadCategoryVocabularyUseCase
) : ViewModel() {
    private val internalState = MutableStateFlow(VocabularyUiState())
    val uiState: StateFlow<VocabularyUiState> = internalState.asStateFlow()

    init { reload() }

    fun reload() {
        internalState.value = runCatching {
            VocabularyUiState(groups = loadVocabulary.execute())
        }.getOrElse {
            VocabularyUiState(catalogError = true)
        }
    }
}
