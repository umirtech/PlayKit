package com.umirtech.playkit

/** Outcome of [PlayUpdateCoordinator.check]. */
sealed interface UpdateCheckResult {
    /** No newer version, or nothing to do. */
    data object UpToDate : UpdateCheckResult

    /** A newer version exists; [flexibleAllowed] tells whether the flexible flow may start. */
    data class UpdateAvailable(val versionCode: Long, val flexibleAllowed: Boolean) : UpdateCheckResult

    /** The flexible update finished downloading prompt a restart. */
    data object UpdateDownloaded : UpdateCheckResult

    /** Play's update APIs are unavailable (side-loaded build, missing Play services, error). */
    data object Unavailable : UpdateCheckResult
}
