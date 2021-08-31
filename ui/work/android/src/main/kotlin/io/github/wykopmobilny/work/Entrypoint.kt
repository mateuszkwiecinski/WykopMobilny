package io.github.wykopmobilny.work

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun WorkManager.setupAllWork(
    repeatMillis: Long,
    flexTimeMillis: Long,
) {
    (0 until RefreshBlacklistRequest.VERSION).forEach { oldVersion ->
        cancelAllWorkByTag("${RefreshBlacklistRequest.WORK_NAME}_v$oldVersion")
    }
    val request = PeriodicWorkRequestBuilder<RefreshBlacklistRequest>(
        repeatMillis, TimeUnit.MILLISECONDS,
        flexTimeMillis, TimeUnit.MILLISECONDS,
    )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(true)
                .build(),
        )
        .addTag("${RefreshBlacklistRequest.WORK_NAME}_v${RefreshBlacklistRequest.VERSION}")
        .build()

    enqueueUniquePeriodicWork(RefreshBlacklistRequest.WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, request)
}
