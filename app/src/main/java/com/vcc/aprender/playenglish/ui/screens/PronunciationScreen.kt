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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vcc.aprender.playenglish.R
import com.vcc.aprender.playenglish.audio.AndroidEnglishPronunciationOutput
import com.vcc.aprender.playenglish.audio.EnglishTtsStatus
import com.vcc.aprender.playenglish.ui.components.ChildScreenLayout
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishColors

@Composable
fun PronunciationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val audio = remember(context) { AndroidEnglishPronunciationOutput(context) }
    DisposableEffect(audio) { onDispose { audio.close() } }

    val status = audio.status
    val message = when (status) {
        EnglishTtsStatus.INITIALIZING -> R.string.pronunciation_tts_loading
        EnglishTtsStatus.READY -> if (audio.lastPlaybackFailed)
            R.string.pronunciation_tts_error else R.string.pronunciation_tts_ready
        EnglishTtsStatus.SPEAKING -> R.string.pronunciation_tts_speaking
        EnglishTtsStatus.VOICE_MISSING -> R.string.pronunciation_tts_unavailable
        EnglishTtsStatus.ERROR, EnglishTtsStatus.CLOSED -> R.string.pronunciation_tts_error
    }
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
            stringResource(message),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.testTag("pronunciation-tts-status")
        )
        if (status == EnglishTtsStatus.VOICE_MISSING) {
            Text(
                stringResource(R.string.pronunciation_tts_install_hint),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("pronunciation-tts-install-hint")
            )
        }
        // Both controls share one engine and one turn gate. During speech
        // neither an ordinary tap nor an accessibility activation may enqueue.
        val canSpeak = status == EnglishTtsStatus.READY && !audio.isSpeaking
        OutlinedButton(
            onClick = { audio.speak("Good morning") },
            enabled = canSpeak,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .testTag("pronunciation-listen")
        ) {
            Text(stringResource(R.string.pronunciation_listen))
        }
        OutlinedButton(
            onClick = { audio.speak("Good morning") },
            enabled = canSpeak,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .testTag("pronunciation-repeat")
        ) {
            Text(stringResource(R.string.pronunciation_repeat))
        }
    }
}
