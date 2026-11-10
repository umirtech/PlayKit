package com.umirtech.playkit

/**
 * Tuning for [PlayKit]. Defaults are conservative: prompts never block the
 * user, and Play's own quotas apply on top of these thresholds.
 */
data class PlayKitConfig(
    /** Master switch for the in-app review prompt. */
    val reviewEnabled: Boolean = true,
    /** Successful runs required before the review flow may be requested. */
    val minSuccessfulRuns: Int = 5,
    /** Days between review attempts. */
    val reviewCooldownDays: Int = 90,
    /** Master switch for the in-app update check. */
    val updateEnabled: Boolean = true,
    /** Days before re-prompting for the same available version. */
    val updatePromptCooldownDays: Int = 3,
)
