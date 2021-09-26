package io.github.wykopmobilny.ui.modules.links.upvoters

import android.app.Activity
import dagger.Binds
import dagger.Module

@Module
abstract class UpvotersModule {

    @Binds
    abstract fun UpvotersActivity.activity(): Activity
}
