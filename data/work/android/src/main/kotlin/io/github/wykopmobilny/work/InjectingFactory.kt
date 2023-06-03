package io.github.wykopmobilny.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.Lazy
import io.github.aakira.napier.Napier
import javax.inject.Inject

class InjectingFactory @Inject constructor(
    private val dependencies: Lazy<WorkDependencies>,
) : WorkerFactory() {

    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
        Napier.i("Creating worker $workerClassName")

        return when (workerClassName) {
            RefreshBlacklistRequest::class.java.name -> RefreshBlacklistRequest(
                context = appContext,
                params = workerParameters,
                getBlacklistRefreshWorkDetails = dependencies.get().blacklistRefresh(),
            )
            CheckNotificationsRequest::class.java.name -> CheckNotificationsRequest(
                context = appContext,
                params = workerParameters,
                notificationRequestRefresh = dependencies.get().notificationsRefresh(),
            )
            else -> null
        }
    }
}
