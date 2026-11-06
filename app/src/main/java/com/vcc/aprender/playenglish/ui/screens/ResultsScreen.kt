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
import com.vcc.aprender.playenglish.domain.model.QuizScore
import com.vcc.aprender.playenglish.ui.components.ChildScreenLayout
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishColors

@Composable
fun ResultsScreen(
    score: QuizScore? = null,
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    ChildScreenLayout(
        title = stringResource(R.string.results_title),
        screenTag = "screen-results",
        onBack = onBack
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PlayEnglishColors.Sky)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("🏆", fontSize = 48.sp)
                Text(
                    stringResource(
                        if (score != null) R.string.results_local_title
                        else R.string.results_placeholder_title
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    if (score != null) stringResource(
                        R.string.results_local_score, score.correctAnswers, score.totalQuestions
                    ) else stringResource(R.string.results_placeholder_body),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                if (score != null) {
                    Text(
                        stringResource(R.string.results_local_note),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Button(
            onClick = onHome,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .testTag("results-home")
        ) {
            Text(stringResource(R.string.results_go_home))
        }
    }
}
