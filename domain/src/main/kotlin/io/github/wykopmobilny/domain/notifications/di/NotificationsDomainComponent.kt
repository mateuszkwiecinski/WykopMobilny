package io.github.wykopmobilny.domain.notifications.di

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import io.github.wykopmobilny.api.responses.NotificationResponse
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.data.storage.api.ReadNotificationEntity
import io.github.wykopmobilny.notification.HandleNotificationDismissed
import io.github.wykopmobilny.notification.NotificationDependencies
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Subcomponent(modules = [NotificationsModule::class])
interface NotificationsDomainComponent : NotificationDependencies

@Module
internal abstract class NotificationsModule {

    @Binds
    abstract fun HandleNotificationDismissedImpl.handleNotificationDismissed(): HandleNotificationDismissed
}

internal class HandleNotificationDismissedImpl @Inject constructor(
    private val appStorage: AppStorage,
    private val store: Store<Int, List<NotificationResponse>>,
) : HandleNotificationDismissed {

    override suspend fun invoke() = withContext(AppDispatchers.IO) {
        val currentNotifications = store.stream(StoreRequest.cached(key = 0, refresh = false))
            .map { it.dataOrNull() }
            .filterNotNull()
            .first()

        appStorage.notificationsQueries.transaction {
            currentNotifications.forEach { notification ->
                appStorage.notificationsQueries.insertOrReplace(
                    ReadNotificationEntity(
                        notificationId = notification.id,
                        dismissedAt = notification.date,
                    ),
                )
            }
        }
    }
}
