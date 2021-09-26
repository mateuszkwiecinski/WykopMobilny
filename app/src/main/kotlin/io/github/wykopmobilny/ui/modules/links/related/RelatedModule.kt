package io.github.wykopmobilny.ui.modules.links.related

import android.app.Activity
import dagger.Binds
import dagger.Module

@Module
abstract class RelatedModule {

    @Binds
    abstract fun RelatedActivity.activity(): Activity
}
