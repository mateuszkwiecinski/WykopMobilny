package io.github.wykopmobilny.work

interface WorkDependencies {

    fun blacklistRefresh(): GetBlacklistRefreshWorkDetails

    fun notificationsRefresh(): GetNotificationsRefreshWorkDetails
}
