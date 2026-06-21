# Dynamic Feature Module — Learning App

A minimal Android app whose **only goal is to demonstrate Play Feature Delivery (on-demand Dynamic Feature Modules)**.

## Module map

```
:app                  — base module (always installed)
:feature_unit         — Unit Converter   (on-demand)
:feature_qr           — QR Generator     (on-demand, ZXing)
:feature_notes        — Quick Notes      (on-demand, Room DB)
:feature_calculator   — Calculator       (on-demand)
```

All four feature modules are bundled into one `.aab`; Play delivers `:app` at install time and each feature only when the user taps "Download".

---

## Why `Run` from Android Studio is not enough

When you press **Run** in Android Studio, `gradle installDebug` pushes the APK directly to the device. In that path:

- All modules are either already merged into the monolithic APK **or** excluded entirely.
- `SplitInstallManager.startInstall()` will return immediately with `sessionId = 0` (already installed) because the features were bundled in, **or** fail because no split APK exists to serve from Play.
- You never see the `PENDING → DOWNLOADING → INSTALLING → INSTALLED` state progression — the whole learning objective of this app.

To observe real on-demand delivery you must use `bundletool`.

---

## Local testing with bundletool

### Prerequisites

Download bundletool from https://github.com/google/bundletool/releases and put it on your `$PATH` (rename to `bundletool`).

### Step 1 — Build the bundle

```bash
./gradlew :app:bundleDebug
# Output: app/build/outputs/bundle/debug/app-debug.aab
```

### Step 2 — Generate device-targeted APK set (local-testing mode)

```bash
bundletool build-apks \
  --local-testing \
  --bundle=app/build/outputs/bundle/debug/app-debug.aab \
  --output=app.apks
```

`--local-testing` embeds the split APKs inside the device's local storage so `SplitInstallManager` can serve them **without contacting the Play Store**. This is the only way to test on-demand delivery locally.

### Step 3 — Install to a connected device

```bash
bundletool install-apks --apks=app.apks
```

bundletool selects the correct configuration split for your device and installs only the base APK. Feature modules remain absent until requested.

### Step 4 — Run the app and watch the DFM flow

1. Open the app — all four cards show **Not installed**.
2. Tap **Download** on any card — the progress bar animates through `Pending → Downloading → Installing`.
3. The card flips to **Installed** — tap **Open** to enter the feature.
4. Tap **Del** on an installed card — the module is deferred-uninstalled; the card returns to **Not installed**.

---

## Alternative: Internal App Sharing (real end-to-end)

For a complete Play Store delivery test:

1. Upload the `.aab` to Play Console → **Internal App Sharing**.
2. Install via the sharing link on a device that has Play Store.
3. On-demand modules are served by Play infrastructure, not local storage.

---

## Key DFM concepts illustrated

| Concept | File(s) |
|---|---|
| SplitCompat in Application | `DynamicFeatureDemoApp.kt` |
| SplitCompat in feature Activity | `UnitConverterActivity.kt` (and each feature) |
| SplitInstallManager lifecycle | `DynamicFeatureViewModel.kt` |
| Session-to-module mapping | `DynamicFeatureViewModel.kt` `activeSessions` map |
| REQUIRES_USER_CONFIRMATION | ViewModel → SharedFlow → `MainActivity.onActivityResult` |
| Launching via reflection | `MainActivity.launchFeature()` |
| Why direct import is impossible | Comment in `Feature.kt` |
| Feature module Gradle setup | Each `feature_*/build.gradle.kts` |
| dist:module manifest element | Each `feature_*/AndroidManifest.xml` |
| Title strings in base module | `app/res/values/strings.xml` |
| DFM-owned Room DB | `feature_notes/data/` |
