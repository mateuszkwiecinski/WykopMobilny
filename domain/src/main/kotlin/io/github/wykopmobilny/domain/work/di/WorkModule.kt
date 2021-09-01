package io.github.wykopmobilny.domain.work.di

import dagger.Binds
import dagger.Module
import io.github.wykopmobilny.domain.work.GetBlacklistRefreshWorkDetailsQuery
import io.github.wykopmobilny.work.GetBlacklistRefreshWorkDetails

@Module
internal abstract class WorkModule {

    @Binds
    abstract fun GetBlacklistRefreshWorkDetailsQuery.bind(): GetBlacklistRefreshWorkDetails
}
