package io.github.wykopmobilny.ui.modules.links.downvoters

import android.app.Activity
import dagger.Binds
import dagger.Module

@Module
abstract class DownvotersModule {

    @Binds
    abstract fun DownvotersActivity.activity(): Activity
}
