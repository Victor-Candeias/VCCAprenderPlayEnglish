package com.vcc.aprender.playenglish.data

import android.content.Context
import com.vcc.aprender.playenglish.domain.model.AgeRange
import com.vcc.aprender.playenglish.domain.model.OfflineVocabularyCatalog
import com.vcc.aprender.playenglish.domain.model.QuizChoice
import com.vcc.aprender.playenglish.domain.model.QuizQuestion
import com.vcc.aprender.playenglish.domain.model.QuizQuestionSpec
import com.vcc.aprender.playenglish.domain.model.WordCategory
import com.vcc.aprender.playenglish.domain.model.WordEntry
import com.vcc.aprender.playenglish.domain.repository.QuizContentRepository
import com.vcc.aprender.playenglish.domain.repository.VocabularyContentRepository
import org.json.JSONArray
import org.json.JSONObject

/**
 * Reads a versioned catalog packaged in the APK. No INTERNET permission,
 * host SDK dependency, filesystem writes or network requests.
 *
 * Throws on malformed content: QuizViewModel converts failures into a safe
 * catalogError state instead of displaying a partial quiz.
 */
class AssetsQuizContentRepository(private val context: Context) :
    QuizContentRepository, VocabularyContentRepository {

    private val bundledCatalog: OfflineVocabularyCatalog by lazy(
        LazyThreadSafetyMode.SYNCHRONIZED
    ) { readBundledCatalog() }

    override fun loadCatalog(): OfflineVocabularyCatalog = bundledCatalog

    override fun loadQuestions(): List<QuizQuestion> {
        val catalog = loadCatalog()
        val words = catalog.words.associateBy { it.id }
        return catalog.questions.map { spec ->
            QuizQuestion(
                id = spec.id,
                prompt = spec.prompt,
                choices = spec.optionIds.map { id ->
                    val entry = checkNotNull(words[id])
                    QuizChoice(entry.id, entry.enGB)
                },
                correctChoiceId = spec.correctId
            )
        }
    }

    private fun readBundledCatalog(): OfflineVocabularyCatalog {
        val json = context.assets.open("content/quiz-v1.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(json)
        require(root.get("schemaVersion") == 1) { "Catalog schemaVersion must be 1." }
        val locales = root.getJSONObject("locales")
        require(requiredString(locales, "source") == "pt-PT" &&
            requiredString(locales, "target") == "en-GB") {
            "Offline catalog locales must be PT-PT and en-GB."
        }

        val words = root.getJSONArray("words").mapValues { obj ->
            val age = obj.getJSONObject("ageRange")
            val imageRef = if (obj.isNull("imageRef")) null
                else requiredString(obj, "imageRef")
            WordEntry(
                id = requiredString(obj, "id"),
                ptPT = requiredString(obj, "ptPT"),
                enGB = requiredString(obj, "enGB"),
                category = WordCategory.fromId(requiredString(obj, "category")),
                ageRange = AgeRange(
                    requiredInt(age, "min"),
                    requiredInt(age, "max")
                ),
                imageRef = imageRef,
                audioText = requiredString(obj, "audioText")
            )
        }
        val questions = root.getJSONArray("questions").mapValues { obj ->
            QuizQuestionSpec(
                id = requiredString(obj, "id"),
                prompt = requiredString(obj, "prompt"),
                optionIds = obj.getJSONArray("optionIds").stringValues(),
                correctId = requiredString(obj, "correctId")
            )
        }
        val catalog = OfflineVocabularyCatalog(words, questions)
        // Non-null image references must resolve to real APK assets.
        catalog.words.forEach { entry ->
            entry.imageRef?.let { path ->
                if (path.startsWith("drawable/")) {
                    require(Regex("drawable/[a-z0-9_]+").matches(path))
                    val id = context.resources.getIdentifier(
                        path.substringAfter("drawable/"), "drawable", context.packageName
                    )
                    require(id != 0) { "Missing bundled vector: $path" }
                    context.resources.getDrawable(id, context.theme)
                } else {
                    require(Regex("images/[a-z0-9_./-]+[.](png|webp|jpg)").matches(path) &&
                        !path.contains(".."))
                    context.assets.open(path).use { stream ->
                        require(stream.read() != -1) { "Bundled image asset must not be empty." }
                    }
                }
            }
        }
        return catalog
    }

    private fun requiredString(obj: JSONObject, key: String): String {
        val value = obj.get(key)
        require(value is String) { "Expected string in catalog field $key." }
        return value
    }

    private fun requiredInt(obj: JSONObject, key: String): Int {
        val value = obj.get(key)
        require(value is Int) { "Expected integer in catalog field $key." }
        return value
    }

    private inline fun <T> JSONArray.mapValues(block: (JSONObject) -> T): List<T> {
        require(length() <= 10_000)
        return (0 until length()).map { index -> block(getJSONObject(index)) }
    }

    private fun JSONArray.stringValues(): List<String> {
        require(length() <= 10_000)
        return (0 until length()).map { index ->
            val value = get(index)
            require(value is String)
            value
        }
    }
}
