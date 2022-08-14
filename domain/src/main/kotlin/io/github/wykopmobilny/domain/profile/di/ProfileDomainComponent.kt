package io.github.wykopmobilny.domain.profile.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.github.wykopmobilny.domain.di.HasScopeInitializer
import io.github.wykopmobilny.domain.profile.ProfileId
import io.github.wykopmobilny.ui.profile.ProfileDependencies

@ProfileScope
@Subcomponent(modules = [ProfileModule::class])
interface ProfileDomainComponent : ProfileDependencies, HasScopeInitializer {

    @Subcomponent.Factory
    interface Factory {

        fun create(
            @BindsInstance @ProfileId
            profileId: String,
        ): ProfileDomainComponent
    }
}
