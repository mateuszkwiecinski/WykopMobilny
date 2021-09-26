package io.github.wykopmobilny.ui.modules.input.entry.add

import android.app.Activity
import dagger.Binds
import dagger.Module

@Module
abstract class AddEntryActivityModule {

    @Binds
    abstract fun AddEntryActivity.activity(): Activity
}
