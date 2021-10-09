package io.github.wykopmobilny.notification

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import dagger.Reusable
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.data.notifications.android.R
import javax.inject.Inject
import kotlin.reflect.KClass

@Reusable
internal class AndroidNotificationManager @Inject constructor(
    private val context: Context,
    private val interopIntentHandler: @JvmSuppressWildcards (AppNotification.Type.Notifications) -> Intent,
) : NotificationsManager {

    private val manager = context.getSystemService<NotificationManager>().let(::checkNotNull)

    override suspend fun upsertNotification(notification: AppNotification) {
        ensureChannels()
        val (id, frameworkNotification) = when (val type = notification.type) {
            is AppNotification.Type.Notifications -> {
                NOTIFICATIONS_NOTIFICATION_ID to NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_GENERAL)
                    .setSmallIcon(R.drawable.ic_wykopmobilny)
                    .setContentTitle(notification.title)
                    .setContentText(notification.message)
                    .setAutoCancel(true)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            context,
                            0,
                            interopIntentHandler(type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                        ),
                    )
                    .build()

            }
        }

        manager.notify(id, frameworkNotification)
    }

    private fun ensureChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannelGroup(
                NotificationChannelGroup(
                    NOTIFICATION_CHANNEL_GROUP_ID,
                    context.getString(R.string.channel_group_notifications),
                ),
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_GENERAL,
                    context.getString(R.string.channel_name_general),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    group = NOTIFICATION_CHANNEL_GROUP_ID
                },
            )
        }
    }

    override suspend fun cancelNotification(type: KClass<out AppNotification.Type>) {
        @Suppress("UseIfInsteadOfWhen")
        val id = when (type) {
            AppNotification.Type.Notifications::class -> NOTIFICATIONS_NOTIFICATION_ID
            else -> return Napier.e("Unknown notification type=$type")
        }

        manager.cancel(id)
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_GROUP_ID = "io.github.wykopmobilny:notifications"
        private const val NOTIFICATION_CHANNEL_GENERAL = "$NOTIFICATION_CHANNEL_GROUP_ID:general"
        private const val NOTIFICATIONS_NOTIFICATION_ID = 123
    }
}

