package io.github.wykopmobilny.domain.linkdetails.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.github.wykopmobilny.domain.di.HasScopeInitializer
import io.github.wykopmobilny.links.details.LinkDetailsDependencies
import io.github.wykopmobilny.links.details.LinkDetailsKey

@LinkDetailsScope
@Subcomponent(modules = [LinkDetailsModule::class])
interface LinkDetailsComponent : LinkDetailsDependencies, HasScopeInitializer {

    @Subcomponent.Factory
    interface Factory {

        fun create(
            @BindsInstance key: LinkDetailsKey,
        ): LinkDetailsComponent
    }
}
