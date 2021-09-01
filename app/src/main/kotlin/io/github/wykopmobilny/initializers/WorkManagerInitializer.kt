package io.github.wykopmobilny.initializers

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.github.wykopmobilny.WykopApp
import io.github.wykopmobilny.utils.requireDependency
import io.github.wykopmobilny.work.InjectingFactory
import io.github.wykopmobilny.work.WorkDependencies
import io.github.wykopmobilny.work.setupAllWork

internal class WorkManagerInitializer : Initializer<WorkManager> {

    override fun create(context: Context): WorkManager {
        val application = context.applicationContext as WykopApp
        val workDependencies = application.requireDependency<WorkDependencies>()

        WorkManager.initialize(
            context,
            Configuration.Builder()
                .setWorkerFactory(
                    InjectingFactory(
                        factory = { workDependencies.blacklistRefresh() },
                    ),
                )
                .build(),
        )
        val workManager = WorkManager.getInstance(context)
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        workManager.setupAllWork(
            repeatMillis = remoteConfig.getLong("wykop_blacklist_refresh_interval"),
            flexTimeMillis = remoteConfig.getLong("wykop_blacklist_flex_interval"),
        )

        return workManager
    }

    override fun dependencies() = listOf(RemoteConfigInitializer::class.java)
}
