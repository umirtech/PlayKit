package com.umirtech.playkit

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Owns the Play in-app review trigger policy.
 *
 * - [onSuccessfulRun] records a finished run and flips [promptPending] when the
 *   configured thresholds are met.
 * - The host app consumes the flag while it has an Activity ([consumePrompt])
 *   and launches the flow with [launchReview].
 * - Every Play failure (side-loaded builds, quotas) is swallowed: the API is
 *   best-effort by design and must never surface an error or a fake UI.
 */
class PlayReviewPrompter internal constructor(
    private val store: PlayKitStateStore,
    private val config: () -> PlayKitConfig,
    private val now: () -> Long,
    private val scope: CoroutineScope,
) {
    private val _promptPending = MutableStateFlow(false)

    /** True when a review prompt is due and the host app should ask Play to show it. */
    val promptPending: StateFlow<Boolean> = _promptPending.asStateFlow()

    /**
     * Records a successful run; flips [promptPending] once the policy allows
     * it. All bookkeeping happens off the UI thread.
     */
    suspend fun onSuccessfulRun() = withContext(Dispatchers.IO) {
        val current = config()
        if (!current.reviewEnabled) return@withContext
        val runs = store.incrementSuccessfulRuns()
        if (ReviewPolicy.shouldPrompt(runs, store.lastReviewAttemptMs(), now(), current)) {
            _promptPending.value = true
        }
    }

    /** Atomically clears the pending flag; true only for the first caller of a pending prompt. */
    fun consumePrompt(): Boolean {
        if (!_promptPending.value) return false
        _promptPending.value = false
        return true
    }

    /**
     * Best-effort launch; records the attempt first, per Play guidelines.
     * Must be called from the main thread [ReviewManager] drives UI.
     */
    fun launchReview(activity: Activity) {
        scope.launch { store.setLastReviewAttemptMs(now()) }
        runCatching {
            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    runCatching {
                        manager.launchReviewFlow(activity, task.result)
                    }.onFailure { Log.w(TAG, "launchReviewFlow unavailable", it) }
                }
            }
        }.onFailure { Log.w(TAG, "review flow unavailable", it) }
    }

    private companion object {
        const val TAG = "PlayKit.Review"
    }
}
