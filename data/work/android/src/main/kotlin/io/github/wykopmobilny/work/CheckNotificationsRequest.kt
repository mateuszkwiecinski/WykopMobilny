package io.github.wykopmobilny.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier

internal class CheckNotificationsRequest(
    context: Context,
    params: WorkerParameters,
    private val notificationRequestRefresh: GetNotificationsRefreshWorkDetails,
) : CoroutineWorker(context, params) {

    override suspend fun doWork() = notificationRequestRefresh().onWorkRequested()
        .onSuccess { Napier.i("notification refresh succeeded") }
        .onFailure { Napier.w("notification refresh failed", it) }
        .fold(
            onSuccess = { Result.success() },
            onFailure = { Result.failure() },
        )

    companion object {
        const val WORK_NAME = "refresh_notifications"
        const val VERSION = 2
    }
}
