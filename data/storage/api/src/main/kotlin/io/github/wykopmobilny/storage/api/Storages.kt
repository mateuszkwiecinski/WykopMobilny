package io.github.wykopmobilny.storage.api

import io.github.wykopmobilny.data.storage.api.AppStorage

interface Storages {

    fun sessionStorage(): SessionStorage

    fun userInfoStorage(): UserInfoStorage

    fun storage(): AppStorage
}
