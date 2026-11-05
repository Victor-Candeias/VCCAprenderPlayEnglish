package com.vcc.aprender.playenglish.plugin

/**
 * Compatibility aliases for PE-006/007 host-session code and existing tests.
 * Scoring lives in the pure Kotlin domain layer from PE-011 onward.
 * No Android or Game Plugin SDK dependency is present in the domain.
 */
typealias QuizCompletion = com.vcc.aprender.playenglish.domain.game.QuizCompletion
typealias QuizProgress = com.vcc.aprender.playenglish.domain.game.QuizProgress
typealias QuizProgressSnapshot = com.vcc.aprender.playenglish.domain.game.QuizProgressSnapshot
