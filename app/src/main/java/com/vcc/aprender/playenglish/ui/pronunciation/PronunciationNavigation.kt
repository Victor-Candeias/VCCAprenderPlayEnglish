package com.vcc.aprender.playenglish.ui.pronunciation

/** Deterministic navigation without wrap-around. */
object PronunciationNavigation {
    fun previous(index: Int, count: Int): Int {
        require(count >= 0)
        return if (count == 0) 0 else (index.coerceIn(0, count - 1) - 1).coerceAtLeast(0)
    }
    fun next(index: Int, count: Int): Int {
        require(count >= 0)
        return if (count == 0) 0 else (index.coerceIn(0, count - 1) + 1).coerceAtMost(count - 1)
    }
}
