package io.github.wykopmobilny.domain.profile.di

import dagger.Binds
import dagger.Module
import io.github.wykopmobilny.domain.profile.GetProfileDetailsQuery
import io.github.wykopmobilny.ui.profile.GetProfileDetails

@Module
internal abstract class ProfileModule {

    @Binds
    abstract fun GetProfileDetailsQuery.getProfileDetails(): GetProfileDetails
}
