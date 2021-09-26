package io.github.wykopmobilny.ui.modules.mikroblog.entry

import android.app.Activity
import dagger.Binds
import dagger.Module

@Module
abstract class EntryDetailModule {

    @Binds
    abstract fun EntryActivity.activity(): Activity
}
