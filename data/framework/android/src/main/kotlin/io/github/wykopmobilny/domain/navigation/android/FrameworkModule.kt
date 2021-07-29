package io.github.wykopmobilny.domain.navigation.android

import dagger.Binds
import dagger.Module
import io.github.wykopmobilny.domain.navigation.AppRestarter
import io.github.wykopmobilny.domain.navigation.NightModeDetector
import io.github.wykopmobilny.domain.navigation.YoutubeAppDetector

@Module
internal abstract class FrameworkModule {

    @Binds
    abstract fun AndroidAppRestarter.appRestarter(): AppRestarter

    @Binds
    abstract fun AndroidNightModeDetector.nightModeDetector(): NightModeDetector

    @Binds
    abstract fun AndroidYoutubeAppDetector.youtubeDetector(): YoutubeAppDetector
}
