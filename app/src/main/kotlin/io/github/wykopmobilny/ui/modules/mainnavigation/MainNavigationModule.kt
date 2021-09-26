package io.github.wykopmobilny.ui.modules.mainnavigation

import android.app.Activity
import dagger.Binds
import dagger.Module

@Module
abstract class MainNavigationModule {

    @Binds
    abstract fun MainNavigationActivity.activity(): Activity
}
