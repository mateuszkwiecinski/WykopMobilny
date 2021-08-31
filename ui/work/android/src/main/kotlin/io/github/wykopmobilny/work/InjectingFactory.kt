package io.github.wykopmobilny.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.Lazy
import io.github.aakira.napier.Napier
import javax.inject.Inject

class InjectingFactory @Inject constructor(
    private val factory: Lazy<GetBlacklistRefreshWorkDetails>,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        Napier.i("Creating worker $workerClassName")
        if (workerClassName != RefreshBlacklistRequest::class.java.name) return null

        return RefreshBlacklistRequest(
            context = appContext,
            params = workerParameters,
            getBlacklistRefreshWorkDetails = factory.get(),
        )
    }
}
