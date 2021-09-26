package io.github.wykopmobilny.ui.modules.profile

import android.app.Activity
import dagger.Binds
import dagger.Module

@Module
abstract class ProfileModule {

    @Binds
    abstract fun ProfileActivity.activity(): Activity
}
