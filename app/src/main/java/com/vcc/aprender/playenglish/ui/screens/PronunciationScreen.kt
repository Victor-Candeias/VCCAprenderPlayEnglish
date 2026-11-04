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

@Composable
fun PronunciationScreen(onBack: () -> Unit) {
    ChildScreenLayout(
        title = stringResource(R.string.pronunciation_title),
        screenTag = "screen-pronunciation",
        onBack = onBack
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PlayEnglishColors.Mint)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("💬", fontSize = 48.sp)
                Text(
                    stringResource(R.string.pronunciation_example_pt),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(R.string.pronunciation_example_en),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
        Text(
            stringResource(R.string.pronunciation_not_ready),
            style = MaterialTheme.typography.bodyLarge
        )
        OutlinedButton(
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .testTag("pronunciation-audio-disabled")
        ) {
            Text(stringResource(R.string.pronunciation_audio_coming))
        }
    }
}
