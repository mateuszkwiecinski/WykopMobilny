package io.github.wykopmobilny.data.cache.sqldelight

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import io.github.wykopmobilny.data.cache.api.ApplicationCache
import javax.inject.Singleton

@Singleton
@Component(modules = [AppCacheModule::class])
interface ApplicationCacheComponent : ApplicationCache {

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): ApplicationCacheComponent
    }
}
