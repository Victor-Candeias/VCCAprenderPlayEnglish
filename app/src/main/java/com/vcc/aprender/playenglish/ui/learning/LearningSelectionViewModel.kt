package com.vcc.aprender.playenglish.ui.learning

import androidx.lifecycle.ViewModel
import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.domain.usecase.LearningCategoryContent
import com.vcc.aprender.playenglish.domain.usecase.LearningLevel
import com.vcc.aprender.playenglish.domain.usecase.SelectLearningContentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Selection belongs to the plugin only; host profile/session/real age never
 * enter this state. ViewModel retains the choice across recompositions/rotation.
 */
data class LearningSelectionUiState(
    val level: LearningLevel = LearningLevel.AGES_3_TO_5,
    val categories: List<LearningCategoryContent> = emptyList(),
    val selectedCategory: WordCategory? = null,
    val catalogError: Boolean = false
) {
    val selectedContent: LearningCategoryContent?
        get() = categories.firstOrNull { it.category == selectedCategory }

    val selectedQuestionIds: List<String>
        get() = selectedContent?.questionIds.orEmpty()

    val hasSelectedCategory: Boolean get() = selectedCategory != null
}

class LearningSelectionViewModel(
    private val selectContent: SelectLearningContentUseCase
) : ViewModel() {
    private val mutableState = MutableStateFlow(LearningSelectionUiState())
    val uiState: StateFlow<LearningSelectionUiState> = mutableState.asStateFlow()

    init { chooseLevel(LearningLevel.AGES_3_TO_5) }

    fun chooseLevel(level: LearningLevel) {
        val previous = mutableState.value
        mutableState.value = runCatching {
            LearningSelectionUiState(
                level = level,
                categories = selectContent.execute(level),
                selectedCategory = previous.selectedCategory
            )
        }.getOrElse {
            // Fail closed: never display partial content or stale question IDs.
            LearningSelectionUiState(level = level, catalogError = true)
        }
    }

    /** Selecting the same category again collapses it. */
    fun chooseCategory(category: WordCategory) {
        val current = mutableState.value
        if (current.catalogError || current.categories.none { it.category == category }) return
        mutableState.value = current.copy(
            selectedCategory = if (current.selectedCategory == category) null else category
        )
    }
}
