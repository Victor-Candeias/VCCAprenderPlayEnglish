package com.vcc.aprender.playenglish.data

import android.content.Context
import com.vcc.aprender.playenglish.domain.model.QuizChoice
import com.vcc.aprender.playenglish.domain.model.QuizQuestion
import com.vcc.aprender.playenglish.domain.repository.QuizContentRepository
import org.json.JSONObject

/**
 * Reads a bundled JSON asset only. No networking, SQLite or host SDK.
 * PE-012 will populate and extend the catalog with words/categories.
 */
class AssetsQuizContentRepository(private val context: Context) : QuizContentRepository {
    override fun loadQuestions(): List<QuizQuestion> {
        val text = context.assets.open("content/quiz-v1.json").bufferedReader(
            Charsets.UTF_8
        ).use { it.readText() }
        val root = JSONObject(text)
        require(root.getInt("schemaVersion") == 1) { "Unsupported offline catalog version." }
        val array = root.getJSONArray("questions")
        require(array.length() <= 10_000)
        val questions = (0 until array.length()).map { index ->
            val objectValue = array.getJSONObject(index)
            val options = objectValue.getJSONArray("options")
            QuizQuestion(
                id = objectValue.getString("id"),
                prompt = objectValue.getString("prompt"),
                choices = (0 until options.length()).map { i ->
                    val option = options.getJSONObject(i)
                    QuizChoice(option.getString("id"), option.getString("label"))
                },
                correctChoiceId = objectValue.getString("correctId")
            )
        }
        require(questions.map { it.id }.toSet().size == questions.size) {
            "Duplicated question ID in offline catalog."
        }
        return questions
    }
}
