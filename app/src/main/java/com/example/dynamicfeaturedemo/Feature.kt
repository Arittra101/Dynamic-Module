package com.example.dynamicfeaturedemo

/**
 * Catalogue of every on-demand feature module shipped in this bundle.
 *
 * [moduleName] must exactly match the Gradle subproject name (without the colon).
 * Play Feature Delivery uses this string to locate the split APK inside the bundle.
 *
 * [activityClass] is the fully-qualified class name launched via reflection.
 * We cannot write `import com.example.feature_unit.UnitConverterActivity` here
 * because :app does not have a compile-time dependency on any feature module —
 * the dependency arrow is ALWAYS feature → base, never base → feature.
 */
enum class Feature(
    val moduleName: String,
    val displayName: String,
    val tagline: String,
    val activityClass: String,
) {
    UNIT_CONVERTER(
        moduleName = "feature_unit",
        displayName = "Unit Converter",
        tagline = "Length · Weight · Temperature",
        activityClass = "com.example.feature_unit.UnitConverterActivity",
    ),
    QR_GENERATOR(
        moduleName = "feature_qr",
        displayName = "QR Generator",
        tagline = "Text → QR code",
        activityClass = "com.example.feature_qr.QrGeneratorActivity",
    ),
    QUICK_NOTES(
        moduleName = "feature_notes",
        displayName = "Quick Notes",
        tagline = "Offline notes with Room DB",
        activityClass = "com.example.feature_notes.NotesActivity",
    ),
    CALCULATOR(
        moduleName = "feature_calculator",
        displayName = "Calculator",
        tagline = "Basic arithmetic",
        activityClass = "com.example.feature_calculator.CalculatorActivity",
    ),
}

/** UI-facing install state emitted by [DynamicFeatureViewModel]. */
sealed interface FeatureState {
    data object NotInstalled : FeatureState
    data object Pending : FeatureState
    data class Downloading(
        val fraction: Float,       // 0f..1f, used for LinearProgressIndicator
        val bytesDownloaded: Long,
        val totalBytes: Long,
    ) : FeatureState
    data object Installing : FeatureState
    data object Installed : FeatureState
    data class Failed(val errorCode: Int) : FeatureState
}
