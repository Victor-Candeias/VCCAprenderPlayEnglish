package com.vcc.aprender.playenglish.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vcc.aprender.playenglish.ui.quiz.QuizViewModel
import com.vcc.aprender.playenglish.ui.quiz.QuizViewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vcc.aprender.playenglish.ui.screens.HomeScreen
import com.vcc.aprender.playenglish.ui.screens.DiscoverScreen
import com.vcc.aprender.playenglish.ui.learning.LearningSelectionViewModel
import com.vcc.aprender.playenglish.ui.learning.LearningSelectionViewModelFactory
import com.vcc.aprender.playenglish.ui.screens.PronunciationScreen
import com.vcc.aprender.playenglish.ui.screens.QuizScreen
import com.vcc.aprender.playenglish.ui.screens.ResultsScreen
import com.vcc.aprender.playenglish.domain.model.QuizPhase
import com.vcc.aprender.playenglish.domain.model.QuizScore
import kotlin.random.Random

@Composable
fun PlayEnglishNavHost(
    onQuizStarted: (List<String>) -> Boolean = { true },
    onAnswerRecorded: (String, Boolean) -> Boolean = { _, _ -> true },
    onQuizCompleted: (QuizScore) -> Unit = {},
    onCancelSession: (() -> Unit)? = null,
    isHostedSession: Boolean = false
) {
    val context = LocalContext.current.applicationContext
    val factory = remember(context) { QuizViewModelFactory(context) }
    val quizViewModel: QuizViewModel = viewModel(factory = factory)
    val quizUiState by quizViewModel.uiState.collectAsState()
    val selectionFactory = remember(context) { LearningSelectionViewModelFactory(context) }
    val selectionViewModel: LearningSelectionViewModel = viewModel(factory = selectionFactory)
    val selectionState by selectionViewModel.uiState.collectAsState()
    // Reset quiz to exactly the IDs allowed by the locally selected age level/category.
    // This also prevents a future interactive screen from starting with all 48 questions.
    LaunchedEffect(selectionState.selectedQuestionIds) {
        quizViewModel.selectQuestionSet(selectionState.selectedQuestionIds, Random.nextLong())
    }
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = PlayEnglishRoutes.HOME
    ) {
        composable(PlayEnglishRoutes.HOME) {
            HomeScreen(
                onExit = onCancelSession,
                onQuiz = { navController.navigate(PlayEnglishRoutes.QUIZ) },
                onDiscover = { navController.navigate(PlayEnglishRoutes.DISCOVER) }
            )
        }
        composable(PlayEnglishRoutes.DISCOVER) {
            DiscoverScreen(
                selectionState = selectionState,
                onLevelSelected = selectionViewModel::chooseLevel,
                onCategorySelected = selectionViewModel::chooseCategory,
                onBack = { onCancelSession?.invoke() ?: navController.popBackStack() },
                onPronunciation = { navController.navigate(PlayEnglishRoutes.PRONUNCIATION) }
            )
        }
        composable(PlayEnglishRoutes.QUIZ) {
            QuizScreen(
                preview = quizUiState.preview,
                catalogError = quizUiState.catalogError,
                selectionState = selectionState,
                onConfirmOption = { questionId, optionId ->
                    val before = quizViewModel.uiState.value.quiz
                    // Only the first real confirmation opens the host progress.
                    // A failed host start prevents an unreported UI answer.
                    val ready = !isHostedSession || before.selected.isNotEmpty() ||
                        onQuizStarted(before.questions.map { it.id })
                    if (ready && quizViewModel.confirmOption(questionId, optionId)) {
                        val answered = quizViewModel.uiState.value.quiz
                        val question = answered.questions.first { it.id == questionId }
                        val correct = optionId == question.correctChoiceId
                        onAnswerRecorded(questionId, correct)
                    }
                },
                onNextPreview = quizViewModel::nextPreviewQuestion,
                // A hosted session cannot silently replace the question deck
                // after recording its first answer (otherwise scores diverge).
                onLevelSelected = { level ->
                    if (!isHostedSession || quizViewModel.uiState.value.quiz.selected.isEmpty())
                        selectionViewModel.chooseLevel(level)
                },
                onCategorySelected = { category ->
                    if (!isHostedSession || quizViewModel.uiState.value.quiz.selected.isEmpty())
                        selectionViewModel.chooseCategory(category)
                },
                onBack = { onCancelSession?.invoke() ?: navController.popBackStack() },
                onConclude = {
                    // Calculate the real score first; sending is explicit on summary.
                    quizViewModel.finishQuiz()
                    if (quizViewModel.uiState.value.quiz.phase == QuizPhase.COMPLETED) {
                        navController.navigate(PlayEnglishRoutes.RESULTS) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(PlayEnglishRoutes.PRONUNCIATION) {
            PronunciationScreen(
                words = selectionState.selectedContent?.words.orEmpty(),
                onBack = { onCancelSession?.invoke() ?: navController.popBackStack() }
            )
        }
        composable(PlayEnglishRoutes.RESULTS) {
            ResultsScreen(
                score = quizUiState.quiz.score,
                onFinishForHost = if (isHostedSession) ({
                    quizUiState.quiz.score?.let(onQuizCompleted)
                }) else null,
                onBack = {
                    onCancelSession?.invoke()
                        ?: navController.popBackStack(PlayEnglishRoutes.HOME, false)
                },
                onHome = { navController.popBackStack(PlayEnglishRoutes.HOME, false) }
            )
        }
    }
}
