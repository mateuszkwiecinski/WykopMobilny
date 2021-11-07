package io.github.wykopmobilny.work

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import io.github.aakira.napier.Napier
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration

internal class WorkManagerScheduler @Inject constructor(
    private val workManager: WorkManager,
) : WorkScheduler {

    override suspend fun setupBlacklistRefresh(
        repeatInterval: Duration,
        flexDuration: Duration,
    ) {
        (1 until RefreshBlacklistRequest.VERSION).forEach { oldVersion ->
            workManager.cancelAllWorkByTag("${RefreshBlacklistRequest.WORK_NAME}_v$oldVersion")
        }
        val request = PeriodicWorkRequestBuilder<RefreshBlacklistRequest>(
            repeatInterval.inWholeMilliseconds, TimeUnit.MILLISECONDS,
            flexDuration.inWholeMilliseconds, TimeUnit.MILLISECONDS,
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

        workManager.enqueueUniquePeriodicWork(RefreshBlacklistRequest.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    override suspend fun setupNotificationsCheck(
        repeatInterval: Duration,
    ) {
        (1 until CheckNotificationsRequest.VERSION).forEach { oldVersion ->
            Napier.i("Cancel CheckNotificationsRequest version=$oldVersion")
            workManager.cancelAllWorkByTag("${CheckNotificationsRequest.WORK_NAME}_v$oldVersion")
        }
        val request = PeriodicWorkRequestBuilder<CheckNotificationsRequest>(
            repeatInterval = repeatInterval.inWholeMilliseconds,
            repeatIntervalTimeUnit = TimeUnit.MILLISECONDS,
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .addTag("${CheckNotificationsRequest.WORK_NAME}_v${CheckNotificationsRequest.VERSION}")
            .build()

        runCatching {
            workManager.enqueueUniquePeriodicWork(
                CheckNotificationsRequest.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
                .await()
        }
            .onFailure { Napier.e("Failed to schedule notifications", it) }
    }

    override suspend fun cancelNotificationsCheck() {
        workManager.cancelUniqueWork(CheckNotificationsRequest.WORK_NAME)
    }
}
