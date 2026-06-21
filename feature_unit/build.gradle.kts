// com.android.dynamic-feature: this module ships inside the .aab but is NOT
// installed with the base app. Play delivers it only when requested at runtime.
// Dependency direction: feature → base (:app). Never base → feature.
plugins {
    alias(libs.plugins.android.dynamic.feature)
    alias(libs.plugins.kotlin.compose)
    // kotlin.android is auto-applied by AGP 9.x for com.android.dynamic-feature
}

android {
    namespace = "com.example.feature_unit"
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
    // Feature modules depend on the base :app, not the other way around.
    // This gives access to shared resources, theme, and the Play feature-delivery library.
    implementation(project(":app"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
