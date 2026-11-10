package com.umirtech.playkit

internal object ReviewPolicy {
    private const val DAY_MS = 24L * 60L * 60L * 1000L

    fun shouldPrompt(
        successfulRuns: Int,
        lastAttemptMs: Long,
        nowMs: Long,
        config: PlayKitConfig,
    ): Boolean {
        if (!config.reviewEnabled) return false
        if (successfulRuns < config.minSuccessfulRuns) return false
        return nowMs - lastAttemptMs >= config.reviewCooldownDays * DAY_MS
    }
}
