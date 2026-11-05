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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vcc.aprender.playenglish.R
import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.domain.usecase.LearningLevel
import com.vcc.aprender.playenglish.ui.components.ChildScreenLayout
import com.vcc.aprender.playenglish.ui.components.LearningFilters
import com.vcc.aprender.playenglish.ui.learning.LearningSelectionUiState
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishColors

/**
 * Offline age-level/category explorer, shared choice with quiz setup.
 * No answer submission or host result is emitted from this screen.
 */
@Composable
fun DiscoverScreen(
    selectionState: LearningSelectionUiState,
    onLevelSelected: (LearningLevel) -> Unit,
    onCategorySelected: (WordCategory) -> Unit,
    onBack: () -> Unit,
    onPronunciation: () -> Unit
) {
    ChildScreenLayout(
        title = stringResource(R.string.discover_title),
        screenTag = "screen-discover",
        onBack = onBack
    ) {
        Text(
            text = stringResource(R.string.discover_intro),
            style = MaterialTheme.typography.bodyLarge
        )

        if (selectionState.catalogError) {
            Text(
                text = stringResource(R.string.discover_error),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag("discover-error")
            )
        } else {
            LearningFilters(
                state = selectionState,
                testPrefix = "discover",
                onLevelSelected = onLevelSelected,
                onCategorySelected = onCategorySelected
            )
            val selected = selectionState.selectedContent
            if (selected != null) {
                if (selected.words.isEmpty()) {
                    Text(
                        stringResource(R.string.learning_no_words),
                        modifier = Modifier.testTag("discover-no-words"),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        selected.words.forEach { word ->
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
