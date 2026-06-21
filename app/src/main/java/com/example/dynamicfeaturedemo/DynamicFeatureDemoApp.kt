package com.example.dynamicfeaturedemo

import android.app.Application
import com.google.android.play.core.splitcompat.SplitCompat

class DynamicFeatureDemoApp : Application() {

    // SplitCompat.install ensures that code and resources from on-demand modules
    // are visible to the running process after they are installed.
    // Without this call, Class.forName / resource lookups for a newly installed
    // feature module will fail until the next process restart.
    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(base)
        SplitCompat.install(this)
    }
}
