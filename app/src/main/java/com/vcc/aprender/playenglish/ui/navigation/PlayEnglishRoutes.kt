package com.vcc.aprender.playenglish.ui.navigation

/** Stable routes; NavHost restores its back stack across configuration changes. */
object PlayEnglishRoutes {
    const val HOME = "home"
    const val QUIZ = "quiz"
    const val PRONUNCIATION = "pronunciation"
    const val RESULTS = "results"

    val all: Set<String> = setOf(HOME, QUIZ, PRONUNCIATION, RESULTS)
}
