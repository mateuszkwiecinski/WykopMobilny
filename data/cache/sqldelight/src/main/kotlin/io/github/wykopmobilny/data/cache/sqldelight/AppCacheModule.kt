package io.github.wykopmobilny.data.cache.sqldelight

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import io.github.wykopmobilny.data.cache.api.AppCache
import javax.inject.Singleton

@Module
internal class AppCacheModule {

    @Singleton
    @Provides
    fun database(context: Context) = AppCache(
        driver = AndroidSqliteDriver(
            schema = AppCache.Schema,
            context = context,
            name = null,
        ),
    )
}
