package com.umirtech.playkit

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewPolicyTest {

    private val day = 24L * 60 * 60 * 1000

    @Test
    fun belowThresholdDoesNotPrompt() {
        assertFalse(ReviewPolicy.shouldPrompt(4, 0L, 100 * day, PlayKitConfig()))
    }

    @Test
    fun thresholdReachedPrompts() {
        assertTrue(ReviewPolicy.shouldPrompt(5, 0L, 100 * day, PlayKitConfig()))
    }

    @Test
    fun cooldownBlocksUntilElapsed() {
        val now = 100 * day
        assertFalse(ReviewPolicy.shouldPrompt(10, now - 89 * day, now, PlayKitConfig()))
        assertTrue(ReviewPolicy.shouldPrompt(10, now - 90 * day, now, PlayKitConfig()))
    }

    @Test
    fun disabledNeverPrompts() {
        assertFalse(ReviewPolicy.shouldPrompt(100, 0L, 999 * day, PlayKitConfig(reviewEnabled = false)))
    }

    @Test
    fun prompterFlipsPendingAfterThresholdAndConsumesOnce() = runTest {
        val store = FakeStore()
        val prompter = PlayReviewPrompter(store, { PlayKitConfig() }, { 100 * day }, this)
        repeat(4) {
            prompter.onSuccessfulRun()
            assertFalse(prompter.promptPending.value)
        }
        prompter.onSuccessfulRun()
        assertTrue(prompter.promptPending.value)
        assertTrue(prompter.consumePrompt())
        assertFalse(prompter.consumePrompt())
    }

    @Test
    fun prompterRespectsRecentAttempt() = runTest {
        val store = FakeStore()
        store.runs = 10
        store.lastReviewAttempt = 100 * day - 10 * day
        val prompter = PlayReviewPrompter(store, { PlayKitConfig() }, { 100 * day }, this)
        prompter.onSuccessfulRun()
        assertFalse(prompter.promptPending.value)
    }
}
