package io.github.wykopmobilny.domain.strings

object Strings {

    const val APP_NAME = "Wykop"

    object Notifications {

        const val TITLE = APP_NAME
        fun notificationContentUnbounded(count: Int) = "Posiadasz $count+ nowych powiadomień."
        fun notificationContent(count: Int) = "Posiadasz $count nowych powiadomień."
    }
}
