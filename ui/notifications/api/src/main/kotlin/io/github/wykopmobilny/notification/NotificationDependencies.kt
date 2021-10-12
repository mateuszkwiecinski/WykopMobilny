package io.github.wykopmobilny.notification

interface NotificationDependencies {

    fun handleNotificationDismissed(): HandleNotificationDismissed
}

interface HandleNotificationDismissed {

    suspend operator fun invoke()
}
