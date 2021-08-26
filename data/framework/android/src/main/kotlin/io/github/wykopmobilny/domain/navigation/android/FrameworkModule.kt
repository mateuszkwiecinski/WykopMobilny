package io.github.wykopmobilny.domain.navigation.android

import dagger.Binds
import dagger.Module
import io.github.wykopmobilny.domain.navigation.AppRestarter
import io.github.wykopmobilny.domain.navigation.SystemSettingsDetector
import io.github.wykopmobilny.domain.navigation.YoutubeAppDetector

@Module
internal abstract class FrameworkModule {

    @Binds
    abstract fun AndroidAppRestarter.appRestarter(): AppRestarter

    @Binds
    abstract fun AndroidSystemSettingsDetector.nightModeDetector(): SystemSettingsDetector

    @Binds
    abstract fun AndroidYoutubeAppDetector.youtubeDetector(): YoutubeAppDetector
}
