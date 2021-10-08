package io.github.wykopmobilny.notification.di

import dagger.Binds
import dagger.Module
import io.github.wykopmobilny.notification.AndroidNotificationManager
import io.github.wykopmobilny.notification.NotificationsManager

@Module
internal abstract class NotificationsModule {

    @Binds
    abstract fun AndroidNotificationManager.bind(): NotificationsManager
}
