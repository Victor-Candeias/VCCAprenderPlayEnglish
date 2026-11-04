package com.vcc.aprender.playenglish.plugin

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vcc.aprender.playenglish.ui.navigation.PlayEnglishNavHost
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishTheme

/**
 * Entry point reserved for VCCAprender; only signed, validated host sessions
 * can open the plugin. The request is not exposed to the standalone launcher.
 *
 * The plugin reports RESULT_OK exactly once *after* a real completed quiz.
 * Until the quiz engine is implemented (PE-015/PE-020), the existing preview
 * cannot invoke onQuizCompleted and closing returns RESULT_CANCELED.
 */
class PlayEnglishPluginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Default and all unsuccessful exits must not be interpreted as scores.
        setResult(RESULT_CANCELED)

        val caller = callingPackage
        val sameSignature = caller == PluginLaunchGate.HOST_PACKAGE &&
            runCatching {
                packageManager.checkSignatures(
                    PluginLaunchGate.HOST_PACKAGE,
                    packageName
                ) == PackageManager.SIGNATURE_MATCH
            }.getOrDefault(false)

        val request = PluginSessionValidator.validate(
            callingPackage = caller,
            signaturesMatch = sameSignature,
            launchIntent = intent,
            expectedComponent = ComponentName(this, PlayEnglishPluginActivity::class.java)
        )
        if (request == null) {
            finish()
            return
        }

        val resultSender = PluginQuizResultSender(request)
        setContent {
            PlayEnglishTheme {
                PlayEnglishNavHost(
                    onQuizCompleted = { completedQuiz ->
                        resultSender.createResultOnce(completedQuiz)?.let { resultIntent ->
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    }
                )
            }
        }
    }
}
