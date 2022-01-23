package io.github.wykopmobilny.domain.navigation.android

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import io.github.wykopmobilny.domain.navigation.AppRestarter
import io.github.wykopmobilny.domain.navigation.ClipboardService
import io.github.wykopmobilny.domain.navigation.WykopTextUtils
import io.github.wykopmobilny.domain.navigation.SystemSettingsDetector
import io.github.wykopmobilny.domain.navigation.AppGateway

@Module
internal abstract class FrameworkModule {

    @Binds
    abstract fun AndroidAppRestarter.appRestarter(): AppRestarter

    @Binds
    abstract fun Application.context(): Context

    @Binds
    abstract fun AndroidSystemSettingsDetector.nightModeDetector(): SystemSettingsDetector

    @Binds
    abstract fun AndroidAppGateway.youtubeDetector(): AppGateway

    @Binds
    abstract fun AndroidWykopTextUtils.htmlUtils(): WykopTextUtils

    @Binds
    abstract fun AndroidClipboardService.clipboardService(): ClipboardService
}
