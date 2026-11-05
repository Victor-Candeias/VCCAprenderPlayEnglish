package com.vcc.aprender.playenglish.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vcc.aprender.playenglish.R
import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.domain.usecase.LearningLevel
import com.vcc.aprender.playenglish.ui.components.ChildScreenLayout
import com.vcc.aprender.playenglish.ui.components.LearningFilters
import com.vcc.aprender.playenglish.ui.learning.LearningSelectionUiState
import com.vcc.aprender.playenglish.ui.quiz.QuizOptionPreview
import com.vcc.aprender.playenglish.ui.quiz.QuizPreviewState
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishColors

/**
 * PE-015: real offline PT-PT question and exactly three EN-GB options.
 * A short tap ONLY previews a choice; it never grades or sends a host result.
 * PE-016 adds long-press confirmation, PE-017/018 feedback, PE-020 result flow.
 *
 * No correctChoiceId enters this composable: it receives a sanitized preview.
 * ChildScreenLayout provides vertical scrolling at 200% font scale.
 */
@Composable
fun QuizScreen(
    preview: QuizPreviewState = QuizPreviewState(),
    catalogError: Boolean = false,
    selectionState: LearningSelectionUiState = LearningSelectionUiState(),
    onLevelSelected: (LearningLevel) -> Unit = {},
    onCategorySelected: (WordCategory) -> Unit = {},
    onPreviewOption: (String, String) -> Unit = { _, _ -> },
    onNextPreview: () -> Unit = {},
    onBack: () -> Unit,
    onPreviewResults: () -> Unit
) {
    // Prevent the previous category's card appearing during LaunchedEffect's
    // async transition to the new filtered quiz question set.
    val question = preview.question?.takeIf {
        it.id in selectionState.selectedQuestionIds &&
            preview.totalQuestions == selectionState.selectedQuestionIds.size
    }
    val hasValidQuestion = question != null && !catalogError && !selectionState.catalogError

    ChildScreenLayout(
        title = stringResource(R.string.quiz_title),
        screenTag = "screen-quiz",
        onBack = onBack
    ) {
        Text(
            stringResource(R.string.quiz_abc_intro),
            style = MaterialTheme.typography.bodyLarge
        )

        if (selectionState.catalogError || catalogError) {
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
                !selectionState.hasSelectedCategory -> Text(
                    stringResource(R.string.learning_choose_category),
                    modifier = Modifier.testTag("quiz-choose-category")
                )
                selected == null || selected.questionCount == 0 -> Text(
                    stringResource(R.string.learning_no_questions),
                    modifier = Modifier.testTag("quiz-no-questions")
                )
                !hasValidQuestion -> Text(
                    stringResource(R.string.quiz_preparing_question),
                    modifier = Modifier.testTag("quiz-preparing-question")
                )
                else -> {
                    val shownQuestion = checkNotNull(question)
                    Text(
                        stringResource(R.string.learning_quiz_count, selected.questionCount),
                        modifier = Modifier.testTag("quiz-selection-count"),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        stringResource(
                            R.string.quiz_question_progress,
                            preview.questionIndex + 1,
                            preview.totalQuestions
                        ),
                        modifier = Modifier.testTag("quiz-question-progress"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = PlayEnglishColors.Yellow
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            QuestionIllustration(
                                category = selectionState.selectedCategory,
                                questionId = shownQuestion.id
                            )
                            Text(
                                shownQuestion.prompt,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                                    .testTag("quiz-question-prompt")
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.quiz_select_preview_instruction),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    shownQuestion.options.forEachIndexed { index, option ->
                        QuizOptionCard(
                            letter = "ABC"[index].toString(),
                            option = option,
                            isSelected = preview.preselectedOptionId == option.id,
                            onClick = { onPreviewOption(shownQuestion.id, option.id) }
                        )
                    }
                    Text(
                        stringResource(
                            if (preview.preselectedOptionId != null)
                                R.string.quiz_preview_selected
                            else R.string.quiz_preview_no_answer
                        ),
                        modifier = Modifier.testTag("quiz-preview-notice"),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = onNextPreview,
                        enabled = preview.canPreviewNext,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                            .testTag("quiz-next-preview")
                    ) {
                        Text(stringResource(R.string.quiz_next_preview))
                    }
                }
            }
        }
        Button(
            onClick = onPreviewResults,
            enabled = hasValidQuestion,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .testTag("quiz-preview-results")
        ) {
            Text(stringResource(R.string.quiz_preview_results))
        }
    }
}

@Composable
private fun QuizOptionCard(
    letter: String,
    option: QuizOptionPreview,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp)
            .testTag("quiz-option-${letter.lowercase()}")
            .semantics {
                stateDescription = if (isSelected)
                    "Pré-selecionada" else "Não selecionada"
            },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) PlayEnglishColors.Mint else Color.White,
            contentColor = PlayEnglishColors.Navy
        ),
        border = BorderStroke(
            if (isSelected) 3.dp else 2.dp,
            if (isSelected) PlayEnglishColors.Green else PlayEnglishColors.RoyalBlue
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(letter, style = MaterialTheme.typography.titleLarge)
            Text(
                option.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

/**
 * Color questions receive a real colored swatch based on their question ID;
 * non-color content shows a category illustration. Neither exposes an EN-GB
 * answer or any correct-option marker before confirmation.
 */
@Composable
private fun QuestionIllustration(category: WordCategory?, questionId: String) {
    val swatch = if (category == WordCategory.COLORS) {
        when (questionId.substringAfterLast('.')) {
            "red" -> Color(0xFFE53935)
            "blue" -> Color(0xFF1E63D2)
            "green" -> Color(0xFF258344)
            "yellow" -> Color(0xFFF9DA35)
            "orange" -> Color(0xFFEF8B25)
            "purple" -> Color(0xFF873ABB)
            "pink" -> Color(0xFFF5A5C4)
            "black" -> Color(0xFF151515)
            "white" -> Color.White
            "brown" -> Color(0xFF90512C)
            "grey" -> Color(0xFF91959B)
            "gold" -> Color(0xFFD4AD30)
            else -> null
        }
    } else null

    if (swatch != null) {
        Box(
            modifier = Modifier.size(80.dp)
                .clip(CircleShape)
                .background(swatch)
                .border(3.dp, PlayEnglishColors.Navy, CircleShape)
                .testTag("quiz-color-swatch")
                .semantics {
                    contentDescription = "Amostra ilustrativa da cor da pergunta"
                }
        )
    } else {
        val emoji = when (category) {
            WordCategory.ANIMALS -> "🐾"
            WordCategory.COLORS -> "🎨"
            WordCategory.NUMBERS -> "🔢"
            WordCategory.FOOD -> "🍎"
            WordCategory.OBJECTS -> "🧸"
            WordCategory.PHRASES -> "💬"
            null -> "🧩"
        }
        Text(
            emoji,
            fontSize = 52.sp,
            modifier = Modifier.testTag("quiz-category-illustration")
                .semantics { contentDescription = "Ilustração da categoria" }
        )
    }
}
