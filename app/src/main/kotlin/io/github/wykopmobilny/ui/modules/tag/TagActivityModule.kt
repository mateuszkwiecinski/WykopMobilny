package io.github.wykopmobilny.ui.modules.tag

import android.app.Activity
import dagger.Binds
import dagger.Module

@Module
abstract class TagActivityModule {

    @Binds
    abstract fun TagActivity.activity(): Activity
}
