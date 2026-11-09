package com.vcc.aprender.playenglish.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vcc.aprender.playenglish.R
import com.vcc.aprender.playenglish.audio.AndroidEnglishPronunciationOutput
import com.vcc.aprender.playenglish.audio.EnglishTtsStatus
import com.vcc.aprender.playenglish.domain.model.WordEntry
import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.domain.usecase.SearchVocabularyUseCase
import kotlin.random.Random
import com.vcc.aprender.playenglish.ui.components.AnimalIllustrations
import com.vcc.aprender.playenglish.ui.components.ChildScreenLayout
import com.vcc.aprender.playenglish.ui.pronunciation.PronunciationNavigation
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishColors

/** PE-023: only real, age/category-filtered offline PT-PT -> en-GB entries. */
@Composable
fun PronunciationScreen(onBack: () -> Unit, words: List<WordEntry> = emptyList()) {
    val context = LocalContext.current
    val audio = remember(context) { AndroidEnglishPronunciationOutput(context) }
    DisposableEffect(audio) { onDispose { audio.close() } }

    // The category/age filter is supplied by the existing selection ViewModel.
    // Query and selected index are screen-local and never mutate the catalog.
    val wordSetKey = words.joinToString("|") { it.id }
    var query by rememberSaveable(wordSetKey) { mutableStateOf("") }
    val visibleWords = remember(words, query) {
        SearchVocabularyUseCase.execute(words, query)
    }
    val resultKey = visibleWords.joinToString("|") { it.id }
    var selectedIndex by rememberSaveable(wordSetKey, resultKey) { mutableIntStateOf(0) }
    val index = if (visibleWords.isEmpty()) 0 else selectedIndex.coerceIn(0, visibleWords.lastIndex)
    val word = visibleWords.getOrNull(index)
    val status = audio.status
    val canSpeak = status == EnglishTtsStatus.READY && !audio.isSpeaking && word != null
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
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(stringResource(R.string.pronunciation_search_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("pronunciation-search")
        )
        if (word == null) {
            Text(
                stringResource(
                    if (query.isBlank()) R.string.pronunciation_no_words
                    else R.string.pronunciation_no_matches
                ),
                modifier = Modifier.testTag("pronunciation-no-words")
            )
        } else {
            Text(
                stringResource(R.string.pronunciation_position, index + 1, visibleWords.size),
                modifier = Modifier.testTag("pronunciation-position"),
                style = MaterialTheme.typography.titleMedium
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PlayEnglishColors.Mint)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val newCategoryIllustration = if (word.category == WordCategory.CLOTHING ||
                        word.category == WordCategory.TRANSPORT ||
                        word.category == WordCategory.FAMILY ||
                        word.category == WordCategory.BODY) {
                        word.imageRef?.takeIf { it.startsWith("drawable/") }?.let { ref ->
                            context.resources.getIdentifier(
                                ref.substringAfter("drawable/"), "drawable", context.packageName
                            )?.takeIf { it != 0 }
                        }
                    } else null
                    if (newCategoryIllustration != null) {
                        Image(
                            painter = painterResource(newCategoryIllustration),
                            contentDescription = "Ilustração: " + word.ptPT,
                            modifier = Modifier.size(112.dp).testTag("pronunciation-illustration")
                        )
                    } else {
                    val illustration = when (word.category) {
                        WordCategory.ANIMALS -> AnimalIllustrations.symbol(word.id)
                        WordCategory.COLORS -> "🎨"
                        WordCategory.NUMBERS -> "🔢"
                        WordCategory.PHRASES -> "💬"
                        WordCategory.FOOD -> "🍎"
                        WordCategory.OBJECTS -> "🧸"
                        WordCategory.CLOTHING -> "👕"
                        WordCategory.TRANSPORT -> "🚆"
                        WordCategory.FAMILY -> "👪"
                        WordCategory.BODY -> "🖐"
                    }
                    Text(illustration, fontSize = 48.sp,
                        modifier = Modifier.testTag("pronunciation-illustration"))
                    }
                    Text(word.ptPT, style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("pronunciation-portuguese"))
                    Text(word.enGB, style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("pronunciation-english"))
                }
            }
        }
        Text(stringResource(message), style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.testTag("pronunciation-tts-status"))
        if (status == EnglishTtsStatus.VOICE_MISSING) {
            Text(stringResource(R.string.pronunciation_tts_install_hint),
                modifier = Modifier.testTag("pronunciation-tts-install-hint"))
        }
        // Only the English audioText is ever passed to the TTS engine.
        // The PE-022 gate makes rapid taps and callbacks safe.
        OutlinedButton(
            onClick = { word?.let { audio.speak(it.audioText, "en-GB") } },
            enabled = canSpeak,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .testTag("pronunciation-listen")
        ) { Text(stringResource(R.string.pronunciation_listen)) }
        OutlinedButton(
            onClick = { word?.let { audio.speak(it.audioText, "en-GB") } },
            enabled = canSpeak,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .testTag("pronunciation-repeat")
        ) { Text(stringResource(R.string.pronunciation_repeat)) }
        if (word != null) {
            OutlinedButton(
                onClick = {
                    val randomValue = Random.nextInt(Int.MAX_VALUE)
                    selectedIndex = SearchVocabularyUseCase.randomIndex(
                        visibleWords.size, index, randomValue
                    )
                },
                enabled = !audio.isSpeaking && visibleWords.size > 1,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    .testTag("pronunciation-random")
            ) { Text(stringResource(R.string.pronunciation_random)) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { selectedIndex = PronunciationNavigation.previous(index, visibleWords.size) },
                    enabled = !audio.isSpeaking && index > 0,
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp)
                        .testTag("pronunciation-previous")
                ) { Text(stringResource(R.string.pronunciation_previous)) }
                OutlinedButton(
                    onClick = { selectedIndex = PronunciationNavigation.next(index, visibleWords.size) },
                    enabled = !audio.isSpeaking && index < visibleWords.lastIndex,
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp)
                        .testTag("pronunciation-next")
                ) { Text(stringResource(R.string.pronunciation_next)) }
            }
        }
    }
}
