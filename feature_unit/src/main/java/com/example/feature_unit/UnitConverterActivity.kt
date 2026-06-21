package com.example.feature_unit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dynamicfeaturedemo.ui.theme.DynamicFeatureDemoTheme
import com.google.android.play.core.splitcompat.SplitCompat

class UnitConverterActivity : ComponentActivity() {

    // SplitCompat.installActivity ensures that on-demand resources (drawables,
    // layouts, strings defined INSIDE this feature module) resolve correctly
    // when the module was installed in the same process session — i.e., without
    // a process restart. Without this call, resource inflation can throw
    // Resources$NotFoundException for resources that live in the split APK.
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DynamicFeatureDemoTheme {
                UnitConverterScreen(onBack = { finish() })
            }
        }
    }
}
