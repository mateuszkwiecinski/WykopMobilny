package io.github.wykopmobilny.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.first

internal class RefreshBlacklistRequest(
    context: Context,
    params: WorkerParameters,
    private val getBlacklistRefreshWorkDetails: GetBlacklistRefreshWorkDetails,
) : CoroutineWorker(context, params) {

    override suspend fun doWork() =
        getBlacklistRefreshWorkDetails().onWorkRequested()
            .onSuccess { Napier.i("blacklist refresh succeeded") }
            .onFailure { Napier.w("blacklist refresh failed", it) }
            .fold(
                onSuccess = { Result.success() },
                onFailure = { Result.failure() },
            )

    companion object {
        const val WORK_NAME = "refresh_blacklist"
        const val VERSION = 1
    }
}
