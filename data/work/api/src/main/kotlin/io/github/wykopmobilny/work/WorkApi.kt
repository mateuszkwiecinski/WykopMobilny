package io.github.wykopmobilny.work

import kotlin.time.Duration

interface WorkApi {

    fun workScheduler(): WorkScheduler
}

interface WorkScheduler {

    suspend fun setupBlacklistRefresh(repeatInterval: Duration, flexDuration: Duration)

    suspend fun setupNotificationsCheck(repeatInterval: Duration)

    suspend fun cancelNotificationsCheck()
}
