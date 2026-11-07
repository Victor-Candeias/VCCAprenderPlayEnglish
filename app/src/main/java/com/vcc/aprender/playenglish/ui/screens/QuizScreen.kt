package com.vcc.aprender.playenglish.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
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
 * PE-016: 800 ms uninterrupted single-pointer hold confirms one answer.
 * A short touch / move / multitouch cancels; keyboard and TalkBack activate
 * an accessible confirmation action without requiring a timed physical hold.
 * PE-017 adds happy feedback only when the confirmed choice is right.
 * PE-018 reveals only the correct option after a wrong confirmation.
 * PE-020 sends host results.
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
    onConfirmOption: (String, String) -> Unit = { _, _ -> },
    onNextPreview: () -> Unit = {},
    onBack: () -> Unit,
    onConclude: () -> Unit
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
                    Text(
                        stringResource(R.string.quiz_correct_progress, preview.correctAnswers),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.testTag("quiz-correct-count")
                    )
                    // After an incorrect answer, physically remove both wrong
                    // alternatives from the composition and accessibility tree.
                    // The correct option is supplied by the domain projection only
                    // after confirmation; its original A/B/C position is retained.
                    val optionsToDisplay = if (preview.wasCorrect == false) {
                        shownQuestion.options.filter { it.id == preview.correctOptionAfterError?.id }
                    } else shownQuestion.options
                    optionsToDisplay.forEach { option ->
                        val index = shownQuestion.options.indexOf(option)
                        QuizOptionCard(
                            letter = "ABC"[index].toString(),
                            option = option,
                            isConfirmed = preview.confirmedOptionId == option.id,
                            showHappySmile = preview.wasCorrect == true &&
                                preview.confirmedOptionId == option.id,
                            isLocked = preview.isConfirmed,
                            onConfirm = { onConfirmOption(shownQuestion.id, option.id) }
                        )
                    }
                    Text(
                        stringResource(
                            if (preview.isConfirmed)
                                R.string.quiz_answer_confirmed
                            else R.string.quiz_waiting_confirmation
                        ),
                        modifier = Modifier.testTag(
                            if (preview.isConfirmed) "quiz-answer-registered"
                            else "quiz-waiting-state"
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (preview.wasCorrect == false) {
                        Text(
                            stringResource(R.string.quiz_wrong_feedback),
                            color = Color(0xFFB3261E),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.testTag("quiz-error-feedback")
                                .semantics { contentDescription = "Resposta errada!" }
                        )
                        preview.correctOptionAfterError?.let { correct ->
                            Text(
                                stringResource(R.string.quiz_wrong_explanation, correct.label),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.testTag("quiz-error-explanation")
                            )
                        }
                    }
                    if (preview.wasCorrect == true) {
                        Text(
                            stringResource(R.string.quiz_correct_feedback),
                            color = PlayEnglishColors.Green,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.testTag("quiz-success-feedback")
                        )
                    }
                    if (preview.canConclude) {
                        Button(
                            onClick = onConclude,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                                .testTag("quiz-conclude")
                        ) {
                            Text(stringResource(R.string.quiz_conclude))
                        }
                    } else {
                        OutlinedButton(
                            onClick = onNextPreview,
                            enabled = preview.canPreviewNext,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                                .testTag("quiz-next-preview")
                        ) {
                            Text(stringResource(R.string.quiz_next_question))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Each touch gesture may confirm only after 800 ms continuously held.
 *
 * Main pass observes scroll consumption, pointer lift, slide and second finger.
 * This is NOT a clickable Button: a short physical tap must do nothing.
 * Semantics onClick/onLongClick and KeyUp allow accessible activation without
 * physically keeping a finger pressed, but use the very same VM guard.
 */
@Composable
private fun QuizOptionCard(
    letter: String,
    option: QuizOptionPreview,
    isConfirmed: Boolean,
    showHappySmile: Boolean,
    isLocked: Boolean,
    onConfirm: () -> Unit
) {
    val enabled = !isLocked
    val currentConfirm by rememberUpdatedState(onConfirm)
    val accessibilityLabel = stringResource(R.string.quiz_accessibility_confirm)
    val modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp)
        .testTag("quiz-option-${letter.lowercase()}")
        .semantics {
            role = Role.Button
            stateDescription = when {
                isConfirmed -> "Resposta registada"
                isLocked -> "Outra resposta já foi confirmada"
                else -> "Manter pressionado 0,8 segundos para confirmar"
            }
            if (enabled) {
                onClick(label = accessibilityLabel) {
                    currentConfirm()
                    true
                }
                onLongClick(label = accessibilityLabel) {
                    currentConfirm()
                    true
                }
            } else {
                disabled()
            }
        }
        .onKeyEvent { keyEvent ->
            if (enabled && keyEvent.type == KeyEventType.KeyUp &&
                (keyEvent.key == Key.Enter || keyEvent.key == Key.Spacebar ||
                    keyEvent.key == Key.DirectionCenter)) {
                currentConfirm()
                true
            } else false
        }
        .focusable(enabled)
        .pointerInput(enabled, option.id) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                var valid = !down.isConsumed
                if (valid) {
                    // Member timeout of AwaitPointerEventScope guarantees that
                    // a short release wins over the 800 ms timer.
                    val releasedOrCanceled = withTimeoutOrNull(800L) {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val current = event.changes.firstOrNull { it.id == down.id }
                            if (current == null ||
                                !current.pressed ||
                                current.isConsumed ||
                                event.changes.any { it.id != down.id && it.pressed } ||
                                (current.position - down.position).getDistance() >
                                    viewConfiguration.touchSlop * 2f) {
                                valid = false
                                break
                            }
                        }
                        true
                    }
                    if (releasedOrCanceled == null && valid) {
                        currentConfirm()
                        // Wait for the active finger to lift. The ViewModel
                        // makes repeated events and accessibility actions inert.
                        waitForUpOrCancellation()
                    }
                }
            }
        }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (isConfirmed) PlayEnglishColors.Mint else Color.White,
        contentColor = PlayEnglishColors.Navy,
        border = BorderStroke(
            if (isConfirmed) 3.dp else 2.dp,
            if (isConfirmed) PlayEnglishColors.Green else PlayEnglishColors.RoyalBlue
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (showHappySmile) {
                Text(
                    "😊",
                    fontSize = 48.sp,
                    modifier = Modifier.testTag("quiz-correct-smile")
                        .semantics { contentDescription = "Resposta certa. Muito bem!" }
                )
            }
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
            "silver" -> Color(0xFFB0BEC5)
            "beige" -> Color(0xFFD9C5A0)
            "turquoise" -> Color(0xFF008C95)
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
    } else if (category == WordCategory.FOOD) {
        val ctx = LocalContext.current
        val name = "pe_food_" + questionId.substringAfterLast('.')
        val resource = ctx.resources.getIdentifier(name, "drawable", ctx.packageName)
        require(resource != 0) { "Missing offline illustration: $name" }
        Image(
            painter = painterResource(resource),
            contentDescription = "Ilustração do alimento",
            modifier = Modifier.size(112.dp).testTag("quiz-food-illustration")
        )
    } else if (category == WordCategory.NUMBERS) {
        // PE-027: visually count cardinal dots or see one geometric figure.
        val id = questionId.substringAfterLast('.')
        val count = when (id) {
            "zero" -> 0
            "one" -> 1
            "two" -> 2
            "three" -> 3
            "four" -> 4
            "five" -> 5
            "six" -> 6
            "seven" -> 7
            "eight" -> 8
            "nine" -> 9
            "ten" -> 10
            else -> null
        }
        val numeral = when (id) {
            "eleven" -> 11
            "twelve" -> 12
            "thirteen" -> 13
            "fourteen" -> 14
            "fifteen" -> 15
            "sixteen" -> 16
            "seventeen" -> 17
            "eighteen" -> 18
            "nineteen" -> 19
            "twenty" -> 20
            "thirty" -> 30
            "forty" -> 40
            "fifty" -> 50
            else -> null
        }
        val shape = when (id) {
            "shape_circle" -> "●"
            "shape_square" -> "■"
            "shape_triangle" -> "▲"
            "shape_rectangle" -> "▰"
            "shape_star" -> "★"
            "shape_diamond" -> "◆"
            "shape_pentagon" -> "⬟"
            "shape_hexagon" -> "⬢"
            else -> null
        }
        val label = when {
            count != null -> if (count == 0) "0" else "●".repeat(count)
            numeral != null -> numeral.toString()
            shape != null -> shape
            else -> "🔢"
        }
        Text(
            label,
            fontSize = if (count != null && count > 5) 27.sp else 52.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
                .testTag("quiz-number-shape-illustration")
                .semantics { contentDescription = "Ilustração do número ou da forma" }
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
