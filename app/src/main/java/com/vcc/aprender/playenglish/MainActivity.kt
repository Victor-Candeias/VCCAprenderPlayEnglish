package com.vcc.aprender.playenglish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vcc.aprender.playenglish.ui.navigation.PlayEnglishNavHost
import com.vcc.aprender.playenglish.ui.theme.PlayEnglishTheme

/**
 * Independent Android APK entry point.
 * Plugin discovery/security will be integrated in PE-003/PE-004.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlayEnglishTheme {
                PlayEnglishNavHost()
            }
        }
    }
}
