package io.github.wykopmobilny.domain.startup

import kotlin.time.Duration

interface AppConfig {

    val blacklistRefreshInterval: Duration
    val blacklistFlexInterval: Duration
    val notificationsEnabled: Boolean
    val youtubeKey: String
}
