package com.umirtech.playkit

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * Wraps Play's in-app update API (flexible flow only the immediate flow is
 * never used, updates must not block the user).
 *
 * Every call is best-effort: side-loaded builds (no Play install) and any Play
 * error surface as [UpdateCheckResult.Unavailable] / `false`, never a crash.
 */
class PlayUpdateCoordinator internal constructor(
    context: Context,
    private val store: PlayKitStateStore,
    private val config: () -> PlayKitConfig,
    private val now: () -> Long,
) {
    private val appContext = context.applicationContext

    private val appUpdateManager get() = AppUpdateManagerFactory.create(appContext)

    /**
     * Checks Play for an update. Runs off the UI thread (policy + Play task +
     * DataStore); the caller can invoke it from any dispatcher.
     *
     * @param applyCooldown true for automatic checks (silently skips while the
     * per-version cooldown runs); false for explicit user-initiated checks.
     */
    suspend fun check(applyCooldown: Boolean = true): UpdateCheckResult = withContext(Dispatchers.IO) {
        if (!config().updateEnabled) return@withContext UpdateCheckResult.Unavailable
        val info = appUpdateManager.appUpdateInfo.awaitOrNull()
            ?: return@withContext UpdateCheckResult.Unavailable
        when {
            info.installStatus() == InstallStatus.DOWNLOADED ->
                UpdateCheckResult.UpdateDownloaded
            info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE ->
                UpdateCheckResult.UpToDate
            else -> {
                val versionCode = info.availableVersionCode().toLong()
                val allowed = !applyCooldown || UpdatePolicy.allowsPrompt(
                    config(), store.lastUpdatePromptVersion(), store.lastUpdatePromptAtMs(), versionCode, now(),
                )
                if (!allowed) {
                    UpdateCheckResult.UpToDate
                } else {
                    store.setUpdatePrompt(versionCode, now())
                    UpdateCheckResult.UpdateAvailable(versionCode, info.isUpdateTypeAllowed(flexibleOptions()))
                }
            }
        }
    }

    /**
     * Starts the flexible update flow through the launcher registered by the
     * host Activity. Must be called from the main thread it drives the
     * Activity result launcher; the Play task wait inside suspends without
     * blocking the UI.
     */
    suspend fun startFlexibleUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>): Boolean {
        val info = appUpdateManager.appUpdateInfo.awaitOrNull() ?: return false
        val options = flexibleOptions()
        if (!info.isUpdateTypeAllowed(options)) return false
        return runCatching {
            appUpdateManager.startUpdateFlowForResult(info, launcher, options)
            true
        }.getOrElse {
            Log.w(TAG, "flexible update unavailable", it)
            false
        }
    }

    /** Completes an already-downloaded flexible update (restarts the app); runs off the UI thread. */
    suspend fun completeUpdate(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            appUpdateManager.completeUpdate()
            true
        }.getOrElse {
            Log.w(TAG, "completeUpdate failed", it)
            false
        }
    }

    private fun flexibleOptions(): AppUpdateOptions =
        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()

    private companion object {
        const val TAG = "PlayKit.Update"

        suspend fun <T> Task<T>.awaitOrNull(): T? = try {
            suspendCancellableCoroutine { cont ->
                addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(task.result)
                    } else {
                        cont.resumeWithException(task.exception ?: RuntimeException("Play task failed"))
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
