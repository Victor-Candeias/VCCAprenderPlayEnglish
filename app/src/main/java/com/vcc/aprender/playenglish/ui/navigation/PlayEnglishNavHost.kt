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
fun PlayEnglishNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = PlayEnglishRoutes.HOME
    ) {
        composable(PlayEnglishRoutes.HOME) {
            HomeScreen(
                onQuiz = { navController.navigate(PlayEnglishRoutes.QUIZ) },
                onPronunciation = {
                    navController.navigate(PlayEnglishRoutes.PRONUNCIATION)
                }
            )
        }
        composable(PlayEnglishRoutes.QUIZ) {
            QuizScreen(
                onBack = { navController.popBackStack() },
                onPreviewResults = { navController.navigate(PlayEnglishRoutes.RESULTS) }
            )
        }
        composable(PlayEnglishRoutes.PRONUNCIATION) {
            PronunciationScreen(onBack = { navController.popBackStack() })
        }
        composable(PlayEnglishRoutes.RESULTS) {
            ResultsScreen(
                onBack = { navController.popBackStack() },
                onHome = { navController.popBackStack(PlayEnglishRoutes.HOME, false) }
            )
        }
    }
}
