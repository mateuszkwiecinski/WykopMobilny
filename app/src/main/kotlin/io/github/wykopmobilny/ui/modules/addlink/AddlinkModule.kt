package io.github.wykopmobilny.ui.modules.addlink

import android.app.Activity
import dagger.Binds
import dagger.Module

@Module
abstract class AddlinkModule {

    @Binds
    abstract fun AddlinkActivity.activity(): Activity
}
