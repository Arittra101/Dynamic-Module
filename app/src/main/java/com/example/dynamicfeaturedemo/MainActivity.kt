package com.example.dynamicfeaturedemo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.dynamicfeaturedemo.ui.HomeScreen
import com.example.dynamicfeaturedemo.ui.theme.DynamicFeatureDemoTheme
import kotlinx.coroutines.launch

// Request code used with SplitInstallManager.startConfirmationDialogForResult.
// Play shows a system dialog when a download exceeds ~10 MB, asking the user to confirm.
private const val REQUEST_CODE_CONFIRM = 101

class MainActivity : ComponentActivity() {

    private val viewModel: DynamicFeatureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Collect REQUIRES_USER_CONFIRMATION events that the ViewModel cannot handle
        // itself (because startConfirmationDialogForResult needs an Activity reference).
        lifecycleScope.launch {
            viewModel.confirmationRequired.collect { state ->
                viewModel.manager.startConfirmationDialogForResult(
                    state, this@MainActivity, REQUEST_CODE_CONFIRM
                )
            }
        }

        setContent {
            DynamicFeatureDemoTheme {
                val states = viewModel.states.collectAsStateWithLifecycle().value
                HomeScreen(
                    featureStates = states,
                    onInstall = viewModel::installModule,
                    onOpen = ::launchFeature,
                    onUninstall = viewModel::uninstallModule,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-sync in case an install completed while the app was in the background.
        viewModel.syncInstalledModules()
    }

    @Deprecated("Needed for SplitInstallManager confirmation dialog result")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CONFIRM && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Download cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Launch a feature module's Activity via reflection.
     *
     * WHY REFLECTION?
     * :app has no compile-time dependency on any feature module — the dependency
     * arrow is always feature → base, never base → feature. If :app imported a
     * class from :feature_unit directly, Gradle would create a circular dependency
     * and the build would fail. Using Intent.setClassName() lets us reference
     * the target class by its string name, resolved at runtime only after the
     * module is installed and SplitCompat has made its classes visible.
     */
    private fun launchFeature(feature: Feature) {
        try {
            val intent = Intent().setClassName(packageName, feature.activityClass)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Should not happen if the UI only shows "Open" when INSTALLED,
            // but guard against races where the user uninstalls from another path.
            Toast.makeText(this, "${feature.displayName} is not available", Toast.LENGTH_SHORT).show()
        }
    }
}
