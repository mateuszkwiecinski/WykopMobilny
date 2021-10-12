package io.github.wykopmobilny.domain.notifications.di

import com.dropbox.android.external.store4.StoreBuilder
import dagger.Module
import dagger.Provides
import io.github.wykopmobilny.api.endpoints.NotificationsRetrofitApi
import io.github.wykopmobilny.domain.api.apiFetcher
import io.github.wykopmobilny.ui.base.AppScopes
import javax.inject.Singleton

@Module
internal class NotificationsGlobalModule {

    @Singleton
    @Provides
    fun notificationsStore(
        retrofitApi: NotificationsRetrofitApi,
        appScopes: AppScopes,
    ) = StoreBuilder.from(
        fetcher = apiFetcher(retrofitApi::getNotifications),
    )
        .scope(appScopes.applicationScope)
        .build()
}
