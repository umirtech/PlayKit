package com.umirtech.playkit

internal class FakeStore : PlayKitStateStore {
    var runs = 0
    var lastReviewAttempt = 0L
    var lastUpdatePromptVersion = 0L
    var lastUpdatePromptAt = 0L

    override suspend fun successfulRuns(): Int = runs

    override suspend fun incrementSuccessfulRuns(): Int {
        runs += 1
        return runs
    }

    override suspend fun lastReviewAttemptMs(): Long = lastReviewAttempt

    override suspend fun setLastReviewAttemptMs(value: Long) {
        lastReviewAttempt = value
    }

    override suspend fun lastUpdatePromptVersion(): Long = lastUpdatePromptVersion

    override suspend fun lastUpdatePromptAtMs(): Long = lastUpdatePromptAt

    override suspend fun setUpdatePrompt(versionCode: Long, atMs: Long) {
        lastUpdatePromptVersion = versionCode
        lastUpdatePromptAt = atMs
    }
}
