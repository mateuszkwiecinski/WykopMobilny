package io.github.wykopmobilny.domain.search.di

import dagger.Binds
import dagger.Module
import io.github.wykopmobilny.domain.search.GetSearchDetailsQuery
import io.github.wykopmobilny.ui.search.GetSearchDetails

@Module
internal abstract class SearchModule {

    @Binds
    abstract fun GetSearchDetailsQuery.getBlacklistDetails(): GetSearchDetails
}
