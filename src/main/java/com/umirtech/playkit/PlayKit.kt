package com.umirtech.playkit

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Entry point for the reusable Play features stack: in-app review prompts and
 * in-app update checks. Typical wiring:
 *
 * ```
 * PlayKit.init(this)                            // Application.onCreate
 * PlayKit.onSuccessfulRun()                     // after a successful run
 * PlayKit.review.promptPending                  // collect while the app is resumed
 * PlayKit.review.consumePrompt() / launchReview(activity)
 * PlayKit.update.check(applyCooldown = true)    // Activity.onResume (automatic)
 * PlayKit.update.check(applyCooldown = false)   // "Check for updates" row (manual)
 * ```
 *
 * Both features are best-effort: builds that were not installed by Play
 * (side-loads, emulators) silently get no-op results.
 */
object PlayKit {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var reviewOrNull: PlayReviewPrompter? = null

    @Volatile
    private var updateOrNull: PlayUpdateCoordinator? = null

    /** In-app review prompter. Throws until [init] was called. */
    val review: PlayReviewPrompter
        get() = reviewOrNull ?: error("PlayKit.init(context, config) not called")

    /** In-app update coordinator. Throws until [init] was called. */
    val update: PlayUpdateCoordinator
        get() = updateOrNull ?: error("PlayKit.init(context, config) not called")

    fun init(context: Context, config: PlayKitConfig = PlayKitConfig()) {
        val appContext = context.applicationContext
        val store = DataStorePlayKitStateStore(appContext)
        reviewOrNull = PlayReviewPrompter(store, { config }, System::currentTimeMillis, scope)
        updateOrNull = PlayUpdateCoordinator(appContext, store, { config }, System::currentTimeMillis)
    }

    /** Records a successful run for the review policy; no-op until [init] was called. */
    fun onSuccessfulRun() {
        val prompter = reviewOrNull ?: return
        scope.launch { prompter.onSuccessfulRun() }
    }
}
