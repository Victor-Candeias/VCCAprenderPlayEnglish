package com.vcc.aprender.playenglish.ui.components

/**
 * PE-026: interim offline system-glyph illustration per animal.
 * PE-036 will replace this with licensed/original packaged vector assets.
 * No answer text is exposed in the quiz's accessibility semantics.
 */
object AnimalIllustrations {
    fun symbol(wordId: String): String = when (wordId.removePrefix("quiz.").removePrefix("animals.")) {
        "cat" -> "🐈"
        "dog" -> "🐕"
        "bird" -> "🐦"
        "fish" -> "🐟"
        "cow" -> "🐄"
        "horse" -> "🐎"
        "sheep" -> "🐑"
        "pig" -> "🐖"
        "duck" -> "🦆"
        "rabbit" -> "🐇"
        "lion" -> "🦁"
        "elephant" -> "🐘"
        "goat" -> "🐐"
        "chicken" -> "🐔"
        "rooster" -> "🐓"
        "mouse" -> "🐭"
        "frog" -> "🐸"
        "bear" -> "🐻"
        "monkey" -> "🐒"
        "giraffe" -> "🦒"
        "zebra" -> "🦓"
        "tiger" -> "🐅"
        "panda" -> "🐼"
        "crocodile" -> "🐊"
        "dolphin" -> "🐬"
        "shark" -> "🦈"
        "octopus" -> "🐙"
        "crab" -> "🦀"
        "whale" -> "🐳"
        "seal" -> "🦭"
        "penguin" -> "🐧"
        "seahorse" -> "🐟"
        else -> "🐾"
    }
}
