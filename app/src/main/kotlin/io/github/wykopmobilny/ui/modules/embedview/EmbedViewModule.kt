package io.github.wykopmobilny.ui.modules.embedview

import android.app.Activity
import dagger.Binds
import dagger.Module

@Module
abstract class EmbedViewModule {

    @Binds
    abstract fun EmbedViewActivity.activity(): Activity
}
