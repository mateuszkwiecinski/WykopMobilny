package io.github.wykopmobilny.ui.modules.links.linkdetails

import android.app.Activity
import dagger.Binds
import dagger.Module

@Module
abstract class LinkDetailsModule {

    @Binds
    abstract fun LinkDetailsActivity.activity(): Activity
}
