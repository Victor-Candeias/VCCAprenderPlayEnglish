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
import com.vcc.aprender.playenglish.ui.components.ChildScreenLayout
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishColors
import com.vcc.aprender.playenglish.ui.quiz.QuizUiState

@Composable
@Suppress("UNUSED_PARAMETER")
fun QuizScreen(
    uiState: QuizUiState = QuizUiState(),
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
        Text(
            stringResource(
                if (uiState.catalogError) R.string.quiz_catalog_error
                else if (!uiState.hasContent) R.string.quiz_catalog_empty
                else R.string.quiz_not_ready
            ),
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            onClick = onPreviewResults,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .testTag("quiz-preview-results")
        ) {
            Text(stringResource(R.string.quiz_preview_results))
        }
    }
}
