package io.github.wykopmobilny.notification

import kotlin.reflect.KClass

interface NotificationsManager {

    suspend fun upsertNotification(notification: AppNotification)

    suspend fun cancelNotification(type: KClass<out AppNotification.Type>)
}

suspend inline fun <reified T : AppNotification.Type> NotificationsManager.cancelNotification() =
    cancelNotification(T::class)
