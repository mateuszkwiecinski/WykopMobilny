package io.github.wykopmobilny.domain.profile.di

import dagger.Module
import dagger.Provides

@Module
class ProfileInputModule(val profileId: String) {

    @Provides
    fun profileId() = profileId
}
