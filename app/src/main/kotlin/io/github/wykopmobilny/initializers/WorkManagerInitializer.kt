package io.github.wykopmobilny.initializers

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import io.github.wykopmobilny.WykopApp
import io.github.wykopmobilny.utils.requireDependency
import io.github.wykopmobilny.work.InjectingFactory

internal class WorkManagerInitializer : Initializer<WorkManager> {

    override fun create(context: Context): WorkManager {
        val application = context.applicationContext as WykopApp

        WorkManager.initialize(
            context,
            Configuration.Builder()
                .setWorkerFactory(
                    InjectingFactory(
                        dependencies = application::requireDependency,
                    ),
                )
                .build(),
        )

        return WorkManager.getInstance(context)
    }

    override fun dependencies(): List<Class<Initializer<*>>> = emptyList()
}
