package com.umirtech.playkit

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.playKitDataStore by preferencesDataStore(name = "playkit")

internal interface PlayKitStateStore {
    suspend fun successfulRuns(): Int
    suspend fun incrementSuccessfulRuns(): Int
    suspend fun lastReviewAttemptMs(): Long
    suspend fun setLastReviewAttemptMs(value: Long)
    suspend fun lastUpdatePromptVersion(): Long
    suspend fun lastUpdatePromptAtMs(): Long
    suspend fun setUpdatePrompt(versionCode: Long, atMs: Long)
}

internal class DataStorePlayKitStateStore(context: Context) : PlayKitStateStore {

    private object Keys {
        val SUCCESSFUL_RUNS = intPreferencesKey("successful_runs")
        val LAST_REVIEW_ATTEMPT_MS = longPreferencesKey("last_review_attempt_ms")
        val LAST_UPDATE_PROMPT_VERSION = longPreferencesKey("last_update_prompt_version")
        val LAST_UPDATE_PROMPT_AT_MS = longPreferencesKey("last_update_prompt_at_ms")
    }

    private val store = context.applicationContext.playKitDataStore

    override suspend fun successfulRuns(): Int =
        store.data.first()[Keys.SUCCESSFUL_RUNS] ?: 0

    override suspend fun incrementSuccessfulRuns(): Int {
        var updated = 0
        store.edit { prefs ->
            updated = (prefs[Keys.SUCCESSFUL_RUNS] ?: 0) + 1
            prefs[Keys.SUCCESSFUL_RUNS] = updated
        }
        return updated
    }

    override suspend fun lastReviewAttemptMs(): Long =
        store.data.first()[Keys.LAST_REVIEW_ATTEMPT_MS] ?: 0L

    override suspend fun setLastReviewAttemptMs(value: Long) {
        store.edit { it[Keys.LAST_REVIEW_ATTEMPT_MS] = value }
    }

    override suspend fun lastUpdatePromptVersion(): Long =
        store.data.first()[Keys.LAST_UPDATE_PROMPT_VERSION] ?: 0L

    override suspend fun lastUpdatePromptAtMs(): Long =
        store.data.first()[Keys.LAST_UPDATE_PROMPT_AT_MS] ?: 0L

    override suspend fun setUpdatePrompt(versionCode: Long, atMs: Long) {
        store.edit {
            it[Keys.LAST_UPDATE_PROMPT_VERSION] = versionCode
            it[Keys.LAST_UPDATE_PROMPT_AT_MS] = atMs
        }
    }
}
