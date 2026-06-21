package com.example.feature_qr

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dynamicfeaturedemo.ui.theme.DynamicFeatureDemoTheme
import com.google.android.play.core.splitcompat.SplitCompat

class QrGeneratorActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DynamicFeatureDemoTheme {
                QrGeneratorScreen(onBack = { finish() })
            }
        }
    }
}
