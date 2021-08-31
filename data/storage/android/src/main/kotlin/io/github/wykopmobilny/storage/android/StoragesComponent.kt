package io.github.wykopmobilny.storage.android

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.storage.api.BlacklistPreferencesApi
import io.github.wykopmobilny.storage.api.LinksPreferencesApi
import io.github.wykopmobilny.storage.api.SessionStorage
import io.github.wykopmobilny.storage.api.Storages
import io.github.wykopmobilny.storage.api.UserInfoStorage
import io.github.wykopmobilny.storage.api.UserPreferenceApi
import java.util.concurrent.Executor
import javax.inject.Singleton

interface InteropUnscopedStorages {

    fun linksPreferences(): LinksPreferencesApi

    fun blacklistPreferences(): BlacklistPreferencesApi

    fun sessionStorage(): SessionStorage

    fun userInfoStorage(): UserInfoStorage

    fun userPreferences(): UserPreferenceApi
}

@Singleton
@Component(modules = [StoragesModule::class])
interface StoragesComponent : InteropUnscopedStorages, Storages {

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance dbName: String?,
            @BindsInstance context: Context,
            @BindsInstance executor: Executor,
        ): StoragesComponent
    }
}

@Module
internal abstract class StoragesModule {

    companion object {

        @Singleton
        @Provides
        fun database(context: Context, name: String?) = AppStorage(
            driver = AndroidSqliteDriver(
                schema = AppStorage.Schema,
                context = context,
                name = name,
            ),
        )
    }

    @Binds
    abstract fun LinksPreferences.provideLinksPreferencesApi(): LinksPreferencesApi

    @Binds
    abstract fun BlacklistPreferences.provideBlacklistApi(): BlacklistPreferencesApi

    @Binds
    abstract fun CredentialsPreferences.sessionStorage(): SessionStorage

    @Binds
    abstract fun CredentialsPreferences.userInfoStorage(): UserInfoStorage

    @Binds
    abstract fun UserPreferences.userPreferences(): UserPreferenceApi
}
