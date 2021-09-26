package io.github.wykopmobilny.domain.linkdetails.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.github.wykopmobilny.domain.di.HasScopeInitializer
import io.github.wykopmobilny.links.details.LinkDetailsDependencies

@LinkDetailsScope
@Subcomponent(modules = [LinkDetailsModule::class])
interface LinkDetailsComponent : LinkDetailsDependencies, HasScopeInitializer {

    @Subcomponent.Factory
    interface Factory {

        fun create(
            @BindsInstance @LinkId
            linkId: Long,
        ): LinkDetailsComponent
    }
}
