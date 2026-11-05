package com.vcc.aprender.playenglish.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vcc.aprender.playenglish.ui.screens.HomeScreen
import com.vcc.aprender.playenglish.ui.screens.PronunciationScreen
import com.vcc.aprender.playenglish.ui.screens.QuizScreen
import com.vcc.aprender.playenglish.ui.screens.ResultsScreen

@Composable
fun PlayEnglishNavHost(
    onQuizStarted: (List<String>) -> Unit = {},
    onAnswerRecorded: (String, Boolean) -> Unit = { _, _ -> },
    onQuizCompleted: () -> Unit = {},
    onCancelSession: (() -> Unit)? = null
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = PlayEnglishRoutes.HOME
    ) {
        composable(PlayEnglishRoutes.HOME) {
            HomeScreen(
                onExit = onCancelSession,
                onQuiz = { navController.navigate(PlayEnglishRoutes.QUIZ) },
                onPronunciation = {
                    navController.navigate(PlayEnglishRoutes.PRONUNCIATION)
                }
            )
        }
        composable(PlayEnglishRoutes.QUIZ) {
            QuizScreen(
                onBack = { onCancelSession?.invoke() ?: navController.popBackStack() },
                onPreviewResults = { navController.navigate(PlayEnglishRoutes.RESULTS) },
                onQuizStarted = onQuizStarted,
                onAnswerRecorded = onAnswerRecorded,
                onQuizCompleted = onQuizCompleted
            )
        }
        composable(PlayEnglishRoutes.PRONUNCIATION) {
            PronunciationScreen(
                onBack = { onCancelSession?.invoke() ?: navController.popBackStack() }
            )
        }
        composable(PlayEnglishRoutes.RESULTS) {
            ResultsScreen(
                onBack = { onCancelSession?.invoke() ?: navController.popBackStack() },
                onHome = { navController.popBackStack(PlayEnglishRoutes.HOME, false) }
            )
        }
    }
}
