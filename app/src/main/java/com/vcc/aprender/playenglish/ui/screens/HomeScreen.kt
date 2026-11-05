package com.vcc.aprender.playenglish.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vcc.aprender.playenglish.R
import com.vcc.aprender.playenglish.ui.components.ChildScreenLayout
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishColors

@Composable
fun HomeScreen(
    onQuiz: () -> Unit,
    onDiscover: () -> Unit,
    onExit: (() -> Unit)? = null
) {
    ChildScreenLayout(
        title = stringResource(R.string.app_name),
        screenTag = "screen-home"
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PlayEnglishColors.Sky),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🇬🇧", fontSize = 52.sp)
                Text(
                    text = stringResource(R.string.home_welcome),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.home_intro),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }

        LearningModeButton(
            emoji = "🧭",
            title = stringResource(R.string.home_discover_title),
            subtitle = stringResource(R.string.home_discover_subtitle),
            containerColor = PlayEnglishColors.Mint,
            tag = "home-discover",
            onClick = onDiscover
        )
        LearningModeButton(
            emoji = "🧩",
            title = stringResource(R.string.home_quiz_title),
            subtitle = stringResource(R.string.home_quiz_subtitle),
            containerColor = PlayEnglishColors.Peach,
            tag = "home-quiz",
            onClick = onQuiz
        )
        if (onExit != null) {
            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    .testTag("session-cancel")
            ) {
                Text(stringResource(R.string.session_cancel))
            }
        }
        Text(
            stringResource(R.string.home_footer),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LearningModeButton(
    emoji: String,
    title: String,
    subtitle: String,
    containerColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 112.dp).testTag(tag),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = PlayEnglishColors.Navy
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
