package com.vcc.aprender.playenglish.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vcc.aprender.playenglish.R
import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.domain.usecase.LearningLevel
import com.vcc.aprender.playenglish.ui.components.ChildScreenLayout
import com.vcc.aprender.playenglish.ui.components.LearningFilters
import com.vcc.aprender.playenglish.ui.learning.LearningSelectionUiState
import com.vcc.aprender.playenglish.ui.quiz.QuizUiState
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishColors

@Composable
@Suppress("UNUSED_PARAMETER")
fun QuizScreen(
    uiState: QuizUiState = QuizUiState(),
    selectionState: LearningSelectionUiState = LearningSelectionUiState(),
    onLevelSelected: (LearningLevel) -> Unit = {},
    onCategorySelected: (WordCategory) -> Unit = {},
    onBack: () -> Unit,
    onPreviewResults: () -> Unit,
    onQuizStarted: (List<String>) -> Unit = {},
    onAnswerRecorded: (String, Boolean) -> Unit = { _, _ -> },
    onQuizCompleted: () -> Unit = {}
) {
    ChildScreenLayout(
        title = stringResource(R.string.quiz_title),
        screenTag = "screen-quiz",
        onBack = onBack
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PlayEnglishColors.Yellow)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🧩", fontSize = 48.sp)
                Text(
                    stringResource(R.string.quiz_preview_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(R.string.quiz_preview_body),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (selectionState.catalogError || uiState.catalogError) {
            Text(
                stringResource(R.string.quiz_catalog_error),
                modifier = Modifier.testTag("quiz-catalog-error"),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LearningFilters(
                state = selectionState,
                testPrefix = "quiz",
                onLevelSelected = onLevelSelected,
                onCategorySelected = onCategorySelected
            )
            val selected = selectionState.selectedContent
            when {
                !selectionState.hasSelectedCategory -> {
                    Text(
                        stringResource(R.string.learning_choose_category),
                        modifier = Modifier.testTag("quiz-choose-category")
                    )
                }
                selected == null || selected.questionCount == 0 -> {
                    Text(
                        stringResource(R.string.learning_no_questions),
                        modifier = Modifier.testTag("quiz-no-questions")
                    )
                }
                else -> {
                    Text(
                        stringResource(R.string.learning_quiz_count, selected.questionCount),
                        modifier = Modifier.testTag("quiz-selection-count"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(R.string.quiz_not_ready),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // PE-015 builds the interactive question cards; never emit a fake score.
        Button(
            onClick = onPreviewResults,
            enabled = !selectionState.catalogError && !uiState.catalogError &&
                selectionState.selectedQuestionIds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .testTag("quiz-preview-results")
        ) {
            Text(stringResource(R.string.quiz_preview_results))
        }
    }
}
