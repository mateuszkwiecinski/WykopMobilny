package io.github.wykopmobilny.ui.modules.notificationslist

import android.app.Activity
import dagger.Binds
import dagger.Module

@Module
abstract class NotificationsListModule {

    @Binds
    abstract fun NotificationsListActivity.activity(): Activity
}
