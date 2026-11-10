package com.umirtech.playkit

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePolicyTest {

    private val day = 24L * 60 * 60 * 1000

    @Test
    fun newVersionAlwaysAllowed() {
        assertTrue(UpdatePolicy.allowsPrompt(PlayKitConfig(), 10L, 100L, 11L, 101L))
    }

    @Test
    fun sameVersionWithinCooldownBlocked() {
        val now = 200 * day
        assertFalse(UpdatePolicy.allowsPrompt(PlayKitConfig(), 10L, now - 2 * day, 10L, now))
    }

    @Test
    fun sameVersionAfterCooldownAllowed() {
        assertTrue(UpdatePolicy.allowsPrompt(PlayKitConfig(), 10L, 0L, 10L, 3 * day))
    }

    @Test
    fun disabledBlocksEverything() {
        assertFalse(UpdatePolicy.allowsPrompt(PlayKitConfig(updateEnabled = false), 0L, 0L, 1L, 999 * day))
    }
}
