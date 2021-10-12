package io.github.wykopmobilny.domain.work

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.fresh
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.api.responses.NotificationResponse
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.notification.AppNotification
import io.github.wykopmobilny.notification.NotificationsManager
import io.github.wykopmobilny.notification.cancelNotification
import io.github.wykopmobilny.storage.api.SessionStorage
import io.github.wykopmobilny.work.GetNotificationsRefreshWorkDetails
import io.github.wykopmobilny.work.WorkData
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal class GetNotificationsRefreshWorkDetailsQuery @Inject constructor(
    private val sessionStorage: SessionStorage,
    private val store: Store<Int, List<NotificationResponse>>,
    private val notificationsManager: NotificationsManager,
    private val appStorage: AppStorage,
) : GetNotificationsRefreshWorkDetails {

    override fun invoke() = run {
        WorkData(
            onWorkRequested = {
                if (sessionStorage.session.first() != null) {
                    runCatching { doRefresh() }
                } else {
                    Napier.i("User not logged in, skipping notification refresh")
                    Result.success(Unit)
                }
            },
        )
    }

    private suspend fun doRefresh() {
        val notifications = store.fresh(key = 0)
        val unreadNotifications = notifications.filter { it.new }
        val readNotifications = appStorage.notificationsQueries.all().executeAsList()
        val newNotifications = unreadNotifications.filterNot { readNotifications.contains(it.id) }

        Napier.i("Notifications refreshed, ${newNotifications.size}(${unreadNotifications.size}) out of ${notifications.size}")
        when (newNotifications.size) {
            0 -> notificationsManager.cancelNotification<AppNotification.Type.Notifications>()
            notifications.size -> notificationsManager.upsertNotification(
                notification = AppNotification(
                    title = "Wykop",
                    message = "Posiadasz ${unreadNotifications.size}+ nowych powiadomień.",
                    type = AppNotification.Type.Notifications.MultipleNotifications,
                ),
            )
            1 -> {
                val notification = newNotifications.first()
                notificationsManager.upsertNotification(
                    notification = AppNotification(
                        title = "Wykop",
                        message = notification.body,
                        type = notification.url?.let {
                            AppNotification.Type.Notifications.SingleMessage(interopUrl = it)
                        } ?: AppNotification.Type.Notifications.MultipleNotifications,
                    ),
                )
            }
            else -> notificationsManager.upsertNotification(
                notification = AppNotification(
                    title = "Wykop",
                    message = "Posiadasz ${unreadNotifications.size} nowych powiadomień.",
                    type = AppNotification.Type.Notifications.MultipleNotifications,
                ),
            )
        }
    }
}
