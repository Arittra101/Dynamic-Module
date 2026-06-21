plugins {
    alias(libs.plugins.android.dynamic.feature)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.feature_qr"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":app"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    // ZXing core: pure-Java QR encoder; no camera permission needed for generation only.
    // We depend only on the core (not the full zxing-android-embedded scanner UI)
    // to keep this module lightweight.
    implementation(libs.zxing.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
