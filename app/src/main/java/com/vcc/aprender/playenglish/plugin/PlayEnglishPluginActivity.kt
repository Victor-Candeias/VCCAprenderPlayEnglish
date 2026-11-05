package com.vcc.aprender.playenglish.plugin

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vcc.aprender.playenglish.ui.navigation.PlayEnglishNavHost
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishTheme

/**
 * Host-only entry point. The host identity, signature and immutable request
 * are checked on every Activity creation, including process restoration.
 */
class PlayEnglishPluginActivity : ComponentActivity() {
    private var session: PluginSessionStateHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Back, cancellation and premature closure must never return a score.
        setResult(RESULT_CANCELED)

        val caller = callingPackage
        val trusted = caller == PluginLaunchGate.HOST_PACKAGE &&
            runCatching {
                packageManager.checkSignatures(
                    PluginLaunchGate.HOST_PACKAGE, packageName
                ) == PackageManager.SIGNATURE_MATCH
            }.getOrDefault(false)

        val request = PluginSessionValidator.validate(
            callingPackage = caller,
            signaturesMatch = trusted,
            launchIntent = intent,
            expectedComponent = ComponentName(this, PlayEnglishPluginActivity::class.java)
        )
        if (request == null) {
            finish()
            return
        }

        val holder = if (savedInstanceState == null) {
            PluginSessionStateHolder.start(request)
        } else {
            val restored = PluginSessionSavedState.read(savedInstanceState, request)
            restored?.let { PluginSessionStateHolder.restore(request, it) }
        }
        // Never revive a canceled or completed session after process death.
        if (holder == null || !holder.isActive) {
            finish()
            return
        }
        session = holder

        setContent {
            PlayEnglishTheme {
                PlayEnglishNavHost(
                    onQuizStarted = { questionIds ->
                        holder.beginQuiz(questionIds)
                    },
                    onAnswerRecorded = { id, correct ->
                        holder.recordAnswer(id, correct)
                    },
                    onQuizCompleted = {
                        holder.finishQuizOnce()?.let { result ->
                            setResult(RESULT_OK, result)
                            finish()
                        }
                    },
                    onCancelSession = {
                        holder.cancel()
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        session?.let {
            PluginSessionSavedState.write(outState, it.snapshot())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        // Do not accept a second launch into an existing session.
        session?.cancel()
        setResult(RESULT_CANCELED)
        finish()
    }
}
