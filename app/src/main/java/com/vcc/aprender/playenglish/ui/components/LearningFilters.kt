package com.vcc.aprender.playenglish.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.vcc.aprender.playenglish.R
import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.domain.usecase.LearningLevel
import com.vcc.aprender.playenglish.ui.learning.LearningSelectionUiState

/**
 * Shared touch-friendly, scroll-parent-compatible level/category controls.
 * Never exposes correct answers, child age or host identity.
 */
@Composable
fun LearningFilters(
    state: LearningSelectionUiState,
    testPrefix: String,
    onLevelSelected: (LearningLevel) -> Unit,
    onCategorySelected: (WordCategory) -> Unit
) {
    Text(
        stringResource(R.string.learning_level_instruction),
        style = MaterialTheme.typography.titleMedium
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LearningLevel.values().forEach { level ->
            val selected = state.level == level
            val label = stringResource(
                if (level == LearningLevel.AGES_3_TO_5) R.string.learning_level_3_5
                else R.string.learning_level_6_8
            )
            val modifier = Modifier.weight(1f).heightIn(min = 56.dp)
                .testTag("$testPrefix-level-${level.minAge}-${level.maxAge}")
                .semantics {
                    stateDescription = if (selected) "Selecionado" else "Não selecionado"
                }
            if (selected) {
                Button(onClick = { onLevelSelected(level) }, modifier = modifier) {
                    Text(label)
                }
            } else {
                OutlinedButton(onClick = { onLevelSelected(level) }, modifier = modifier) {
                    Text(label)
                }
            }
        }
    }
    Text(
        stringResource(R.string.learning_category_instruction),
        style = MaterialTheme.typography.titleMedium
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        state.categories.forEach { content ->
            val selected = content.category == state.selectedCategory
            val label = stringResource(
                R.string.learning_category_counts,
                learningCategoryName(content.category),
                content.wordCount,
                content.questionCount
            )
            val modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)
                .testTag("$testPrefix-category-${content.category.id}")
                .semantics {
                    stateDescription = if (selected) "Selecionado" else "Não selecionado"
                }
            if (selected) {
                Button(
                    onClick = { onCategorySelected(content.category) },
                    modifier = modifier
                ) { Text(label, style = MaterialTheme.typography.bodyLarge) }
            } else {
                OutlinedButton(
                    onClick = { onCategorySelected(content.category) },
                    modifier = modifier
                ) { Text(label, style = MaterialTheme.typography.bodyLarge) }
            }
        }
    }
}

@Composable
fun learningCategoryName(category: WordCategory): String {
    val resId = when (category) {
        WordCategory.ANIMALS -> R.string.category_animals
        WordCategory.COLORS -> R.string.category_colors
        WordCategory.NUMBERS -> R.string.category_numbers
        WordCategory.FOOD -> R.string.category_food
        WordCategory.OBJECTS -> R.string.category_objects
        WordCategory.PHRASES -> R.string.category_phrases
    }
    return stringResource(resId)
}
