package io.github.wykopmobilny.work.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import io.github.wykopmobilny.work.WorkApi
import io.github.wykopmobilny.work.WorkManagerScheduler
import io.github.wykopmobilny.work.WorkScheduler

@Component(modules = [WorkManagerModule::class])
interface WorkManagerComponent : WorkApi {

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): WorkManagerComponent
    }
}

@Module
internal abstract class WorkManagerModule {

    companion object {

        @Provides
        fun workManager(context: Context) = WorkManager.getInstance(context)
    }

    @Binds
    abstract fun WorkManagerScheduler.bind(): WorkScheduler
}
