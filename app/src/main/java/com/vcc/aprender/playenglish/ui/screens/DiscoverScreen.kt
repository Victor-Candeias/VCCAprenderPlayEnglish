package com.vcc.aprender.playenglish.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vcc.aprender.playenglish.R
import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.ui.components.ChildScreenLayout
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishColors
import com.vcc.aprender.playenglish.ui.vocabulary.VocabularyUiState

/**
 * Offline vocabulary explorer. PE-014 will add age/level filters.
 * Not a quiz: this screen never emits answers or a host result.
 */
@Composable
fun DiscoverScreen(
    uiState: VocabularyUiState,
    onBack: () -> Unit,
    onPronunciation: () -> Unit
) {
    var expandedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    ChildScreenLayout(
        title = stringResource(R.string.discover_title),
        screenTag = "screen-discover",
        onBack = onBack
    ) {
        Text(
            text = stringResource(R.string.discover_intro),
            style = MaterialTheme.typography.bodyLarge
        )

        if (uiState.catalogError) {
            Text(
                text = stringResource(R.string.discover_error),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag("discover-error")
            )
        } else {
            uiState.groups.forEach { group ->
                val isOpen = expandedCategory == group.category.id
                OutlinedButton(
                    onClick = {
                        expandedCategory = if (isOpen) null else group.category.id
                    },
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("discover-category-${group.category.id}")
                ) {
                    Text(
                        text = stringResource(
                            R.string.discover_category_count,
                            categoryLabel(group.category),
                            group.words.size
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (isOpen) {
                    if (group.words.isEmpty()) {
                        Text(stringResource(R.string.discover_empty))
                    } else {
                        group.words.forEach { word ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                                    .testTag("discover-word-${word.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = PlayEnglishColors.Sky
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        word.ptPT,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        word.enGB,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onPronunciation,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .testTag("discover-pronunciation")
        ) {
            Text(stringResource(R.string.discover_pronunciation))
        }
    }
}

@Composable
private fun categoryLabel(category: WordCategory): String {
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
