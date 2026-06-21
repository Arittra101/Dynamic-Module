// Top-level build file. Only declare plugins here (apply false); each module opts in.
// Note: kotlin.android is NOT declared here because AGP 9.x auto-applies it for both
// com.android.application and com.android.dynamic-feature. Declaring it again causes
// "extension 'kotlin' already registered" at configuration time.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.dynamic.feature) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
