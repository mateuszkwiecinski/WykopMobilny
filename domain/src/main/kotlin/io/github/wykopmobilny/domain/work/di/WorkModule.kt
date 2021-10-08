package io.github.wykopmobilny.domain.work.di

import dagger.Binds
import dagger.Module
import io.github.wykopmobilny.domain.work.GetBlacklistRefreshWorkDetailsQuery
import io.github.wykopmobilny.domain.work.GetNotificationsRefreshWorkDetailsQuery
import io.github.wykopmobilny.work.GetBlacklistRefreshWorkDetails
import io.github.wykopmobilny.work.GetNotificationsRefreshWorkDetails

@Module
internal abstract class WorkModule {

    @Binds
    abstract fun GetBlacklistRefreshWorkDetailsQuery.bindBlacklist(): GetBlacklistRefreshWorkDetails

    @Binds
    abstract fun GetNotificationsRefreshWorkDetailsQuery.bindNotifications(): GetNotificationsRefreshWorkDetails
}
