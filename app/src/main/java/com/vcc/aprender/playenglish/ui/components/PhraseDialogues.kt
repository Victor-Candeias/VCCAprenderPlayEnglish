package com.vcc.aprender.playenglish.ui.components

import android.content.Context
import com.vcc.aprender.playenglish.domain.model.WordEntry
import org.json.JSONObject

data class PhraseDialogue(
    val id: String,
    val title: String,
    val turns: List<WordEntry>
)

/**
 * Curated offline conversations referencing stable catalog word identifiers.
 * Only dialogues whose two turns belong to the current age-filtered selection are shown.
 */
object PhraseDialogues {
    fun load(context: Context, words: List<WordEntry>): List<PhraseDialogue> {
        val byId = words.associateBy { it.id }
        val root = JSONObject(
            context.assets.open("content/dialogues-v1.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        )
        require(root.getInt("schemaVersion") == 1)
        require(root.getString("category") == "phrases")
        val entries = root.getJSONArray("dialogs")
        return (0 until entries.length()).mapNotNull { index ->
            val entry = entries.getJSONObject(index)
            val turns = entry.getJSONArray("turnIds")
            require(turns.length() == 2)
            val first = byId[turns.getString(0)]
            val second = byId[turns.getString(1)]
            if (first == null || second == null) null
            else PhraseDialogue(entry.getString("id"), entry.getString("title"), listOf(first, second))
        }
    }
}
