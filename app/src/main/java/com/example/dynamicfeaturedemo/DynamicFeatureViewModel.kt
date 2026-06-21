package com.example.dynamicfeaturedemo

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DynamicFeatureViewModel(application: Application) : AndroidViewModel(application) {

    // SplitInstallManager is the Play Feature Delivery entry point.
    // It is lightweight; create one per logical owner (here: the ViewModel).
    val manager = SplitInstallManagerFactory.create(application)

    // Persists pending deferred-uninstall requests across process death.
    // deferredUninstall() only executes when the device is idle and the app is not running,
    // so the APK stays physically present until then. Without this set, syncInstalledModules()
    // on the next launch would see the APK still there and flip the UI back to Installed.
    private val prefs = application.getSharedPreferences("dfm_prefs", Context.MODE_PRIVATE)
    private val pendingUninstallKey = "pending_uninstall"

    private fun savePendingUninstall(feature: Feature) {
        val current = prefs.getStringSet(pendingUninstallKey, emptySet())!!.toMutableSet()
        current.add(feature.moduleName)
        prefs.edit().putStringSet(pendingUninstallKey, current).apply()
    }

    private fun clearPendingUninstall(feature: Feature) {
        val current = prefs.getStringSet(pendingUninstallKey, emptySet())!!.toMutableSet()
        current.remove(feature.moduleName)
        prefs.edit().putStringSet(pendingUninstallKey, current).apply()
    }

    private fun pendingUninstalls(): Set<String> =
        prefs.getStringSet(pendingUninstallKey, emptySet())!!.toSet()

    // Per-feature install state exposed to the UI via Compose's collectAsStateWithLifecycle.
    private val _states = MutableStateFlow(
        Feature.entries.associateWith<Feature, FeatureState> { FeatureState.NotInstalled }
    )
    val states: StateFlow<Map<Feature, FeatureState>> = _states.asStateFlow()

    // Maps active session IDs → Feature so we know which module a status update belongs to.
    // Multiple features can be downloading concurrently; each gets its own session.
    private val activeSessions = mutableMapOf<Int, Feature>()

    // When Play requires user confirmation (large module download), we emit the
    // raw SplitInstallSessionState to the Activity so it can call
    // manager.startConfirmationDialogForResult(), which needs an Activity reference.
    private val _confirmationRequired = MutableSharedFlow<SplitInstallSessionState>(extraBufferCapacity = 1)
    val confirmationRequired: SharedFlow<SplitInstallSessionState> = _confirmationRequired.asSharedFlow()

    private val listener = SplitInstallStateUpdatedListener { state ->
        val feature = activeSessions[state.sessionId()] ?: return@SplitInstallStateUpdatedListener
        when (state.status()) {
            SplitInstallSessionStatus.PENDING ->
                setState(feature, FeatureState.Pending)

            SplitInstallSessionStatus.DOWNLOADING -> {
                val total = state.totalBytesToDownload()
                val fraction = if (total > 0) state.bytesDownloaded().toFloat() / total else 0f
                setState(feature, FeatureState.Downloading(fraction, state.bytesDownloaded(), total))
            }

            // DOWNLOADED means the split APK is on disk but not yet dex-opt'd / merged.
            SplitInstallSessionStatus.DOWNLOADED ->
                setState(feature, FeatureState.Installing)

            SplitInstallSessionStatus.INSTALLING ->
                setState(feature, FeatureState.Installing)

            SplitInstallSessionStatus.INSTALLED -> {
                setState(feature, FeatureState.Installed)
                activeSessions.remove(state.sessionId())
            }

            SplitInstallSessionStatus.FAILED -> {
                setState(feature, FeatureState.Failed(state.errorCode()))
                activeSessions.remove(state.sessionId())
            }

            SplitInstallSessionStatus.CANCELED -> {
                setState(feature, FeatureState.NotInstalled)
                activeSessions.remove(state.sessionId())
            }

            // Play shows a system dialog asking the user to confirm a large download.
            // We cannot show that dialog from a ViewModel (we need an Activity), so
            // we forward the raw state to the Activity via SharedFlow.
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION ->
                _confirmationRequired.tryEmit(state)
        }
    }

    init {
        manager.registerListener(listener)
        // On every ViewModel creation, sync state with modules already installed
        // (e.g. after the user killed the app mid-download in a previous session).
        syncInstalledModules()
    }

    /** Kick off an on-demand install for the given feature. */
    fun installModule(feature: Feature) {
        clearPendingUninstall(feature)
        if (manager.installedModules.contains(feature.moduleName)) {
            setState(feature, FeatureState.Installed)
            return
        }
        val request = SplitInstallRequest.newBuilder()
            .addModule(feature.moduleName)
            .build()

        manager.startInstall(request)
            .addOnSuccessListener { sessionId ->
                // sessionId is 0 when the module was already installed;
                // otherwise it identifies this download session.
                if (sessionId == 0) {
                    setState(feature, FeatureState.Installed)
                } else {
                    activeSessions[sessionId] = feature
                    setState(feature, FeatureState.Pending)
                }
            }
            .addOnFailureListener { exception ->
                val code = (exception as? SplitInstallException)?.errorCode ?: -1
                setState(feature, FeatureState.Failed(code))
            }
    }

    /**
     * Schedule deferred uninstall. Play removes the split APK when the device
     * is idle and the app is not running. The UI updates immediately to NotInstalled
     * so the user gets instant feedback (the module is no longer accessible anyway).
     */
    fun uninstallModule(feature: Feature) {
        savePendingUninstall(feature)
        manager.deferredUninstall(listOf(feature.moduleName))
            .addOnSuccessListener { setState(feature, FeatureState.NotInstalled) }
            .addOnFailureListener { setState(feature, FeatureState.NotInstalled) }
    }

    /** Read the authoritative installed-module set from the manager and sync. */
    fun syncInstalledModules() {
        val installed = manager.installedModules
        val pendingRemoval = pendingUninstalls()
        _states.update { current ->
            current.toMutableMap().apply {
                Feature.entries.forEach { feature ->
                    when {
                        // Deferred uninstall was requested but APK not yet removed by OS.
                        // Keep showing NotInstalled so the UI doesn't flip back.
                        feature.moduleName in pendingRemoval && feature.moduleName !in installed -> {
                            clearPendingUninstall(feature)
                            this[feature] = FeatureState.NotInstalled
                        }
                        feature.moduleName in pendingRemoval -> {
                            this[feature] = FeatureState.NotInstalled
                        }
                        feature.moduleName in installed && current[feature] !is FeatureState.Downloading -> {
                            this[feature] = FeatureState.Installed
                        }
                    }
                }
            }
        }
    }

    private fun setState(feature: Feature, state: FeatureState) {
        _states.update { it.toMutableMap().also { m -> m[feature] = state } }
    }

    override fun onCleared() {
        manager.unregisterListener(listener)
    }
}
