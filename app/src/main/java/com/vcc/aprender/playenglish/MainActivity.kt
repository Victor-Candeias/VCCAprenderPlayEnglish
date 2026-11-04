package com.vcc.aprender.playenglish

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

/**
 * Temporary standalone entry point for PE-001.
 * No plugin discovery/host contract until PE-003 and PE-004.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val horizontalPadding = (24 * resources.displayMetrics.density).toInt()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(horizontalPadding, horizontalPadding, horizontalPadding, horizontalPadding)
        }
        content.addView(TextView(this).apply {
            text = getString(R.string.welcome_title)
            textSize = 28f
            gravity = Gravity.CENTER
        })
        content.addView(TextView(this).apply {
            text = getString(R.string.welcome_message)
            textSize = 18f
            gravity = Gravity.CENTER
        })
        setContentView(content)
    }
}
