package io.github.wykopmobilny.storage.api

import io.github.wykopmobilny.data.storage.api.AppStorage

interface Storages {

    fun linksPreferences(): LinksPreferencesApi

    fun blacklistPreferences(): BlacklistPreferencesApi

    fun sessionStorage(): SessionStorage

    fun userInfoStorage(): UserInfoStorage

    fun userPreferences(): UserPreferenceApi

    fun storage(): AppStorage
}
