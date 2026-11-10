package com.umirtech.playkit

internal object UpdatePolicy {
    private const val DAY_MS = 24L * 60L * 60L * 1000L

    fun allowsPrompt(
        config: PlayKitConfig,
        lastPromptVersion: Long,
        lastPromptAtMs: Long,
        versionCode: Long,
        nowMs: Long,
    ): Boolean {
        if (!config.updateEnabled) return false
        if (versionCode != lastPromptVersion) return true
        return nowMs - lastPromptAtMs >= config.updatePromptCooldownDays * DAY_MS
    }
}
