package io.github.wykopmobilny.notification

data class AppNotification(
    val title: String,
    val message: String,
    val type: Type,
) {

    sealed class Type {

        sealed class Notifications : Type() {
            object MultipleNotifications : Notifications()
            data class SingleMessage(val interopUrl: String) : Notifications()
        }
    }
}
