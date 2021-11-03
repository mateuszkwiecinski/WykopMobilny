package io.github.wykopmobilny.domain.work

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.fresh
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.api.responses.NotificationResponse
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.strings.Strings
import io.github.wykopmobilny.notification.AppNotification
import io.github.wykopmobilny.notification.NotificationsManager
import io.github.wykopmobilny.notification.cancelNotification
import io.github.wykopmobilny.storage.api.SessionStorage
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.work.GetNotificationsRefreshWorkDetails
import io.github.wykopmobilny.work.WorkData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class GetNotificationsRefreshWorkDetailsQuery @Inject constructor(
    private val sessionStorage: SessionStorage,
    private val store: Store<Int, List<NotificationResponse>>,
    private val notificationsManager: NotificationsManager,
    private val appStorage: AppStorage,
) : GetNotificationsRefreshWorkDetails {

    override fun invoke() =
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

    private suspend fun doRefresh() {
        val notifications = store.fresh(key = 0)
        val unreadNotifications = notifications.filter(NotificationResponse::new)
        val newNotifications = unreadNotifications.filter {
            withContext(AppDispatchers.IO) {
                val dismissedEntry = appStorage.notificationsQueries.getById(it.id).executeAsOneOrNull()
                    ?: return@withContext true
                dismissedEntry.dismissedAt > it.date
            }
        }

        Napier.i("Notifications refreshed, ${newNotifications.size}(${unreadNotifications.size}) out of ${notifications.size}")
        when (newNotifications.size) {
            0 -> notificationsManager.cancelNotification<AppNotification.Type.Notifications>()
            notifications.size -> notificationsManager.upsertNotification(
                notification = AppNotification(
                    title = Strings.Notifications.TITLE,
                    message = Strings.Notifications.notificationContentUnbounded(unreadNotifications.size),
                    type = AppNotification.Type.Notifications.MultipleNotifications,
                ),
            )
            1 -> {
                val notification = newNotifications.first()
                notificationsManager.upsertNotification(
                    notification = AppNotification(
                        title = Strings.Notifications.TITLE,
                        message = notification.body,
                        type = notification.url?.let {
                            AppNotification.Type.Notifications.SingleMessage(interopUrl = it)
                        } ?: AppNotification.Type.Notifications.MultipleNotifications,
                    ),
                )
            }
            else -> notificationsManager.upsertNotification(
                notification = AppNotification(
                    title = Strings.Notifications.TITLE,
                    message = Strings.Notifications.notificationContent(unreadNotifications.size),
                    type = AppNotification.Type.Notifications.MultipleNotifications,
                ),
            )
        }
    }
}
