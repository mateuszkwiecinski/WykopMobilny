package io.github.wykopmobilny.domain.di

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dagger.Module
import dagger.Provides
import io.github.wykopmobilny.api.endpoints.LoginRetrofitApi
import io.github.wykopmobilny.api.responses.LoginResponse
import io.github.wykopmobilny.blacklist.api.ScraperRetrofitApi
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.api.apiFetcher
import io.github.wykopmobilny.storage.api.Blacklist
import io.github.wykopmobilny.storage.api.LoggedUserInfo
import io.github.wykopmobilny.storage.api.UserInfoStorage
import io.github.wykopmobilny.storage.api.UserSession
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.AppScopes
import kotlinx.coroutines.flow.combine
import javax.inject.Singleton

@Module
internal class StoresModule {

    @Singleton
    @Provides
    fun blacklistStore(
        retrofitApi: ScraperRetrofitApi,
        storage: AppStorage,
        appScopes: AppScopes,
    ) = StoreBuilder.from<Unit, Blacklist, Blacklist>(
        fetcher = Fetcher.of {
            val api = retrofitApi.getBlacklist()
            Blacklist(
                tags = api.tags?.tags?.mapNotNull { it.tag?.removePrefix("#") }?.toSet().orEmpty(),
                users = api.users?.users?.mapNotNull { it.nick?.removePrefix("@") }?.toSet().orEmpty(),
            )
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = {
                combine(
                    storage.blacklistQueries.allTags().asFlow().mapToList(AppDispatchers.Default),
                    storage.blacklistQueries.allProfiles().asFlow().mapToList(AppDispatchers.Default),
                ) { tags, profiles ->
                    Blacklist(
                        tags = tags.toSet(),
                        users = profiles.toSet(),
                    )
                }
            },
            writer = { _, newValue ->
                storage.blacklistQueries.transaction {
                    storage.blacklistQueries.deleteAll()
                    newValue.tags.forEach(storage.blacklistQueries::insertOrReplaceTag)
                    newValue.users.forEach(storage.blacklistQueries::insertOrReplaceProfile)
                }
            },
            delete = { error("unsupported") },
            deleteAll = {
                storage.blacklistQueries.transaction {
                    storage.blacklistQueries.deleteAll()
                }
            },
        ),
    )
        .scope(appScopes.applicationScope)
        .build()

    @Singleton
    @Provides
    fun loginStore(
        retrofitApi: LoginRetrofitApi,
        storage: UserInfoStorage,
        appScopes: AppScopes,
    ) = StoreBuilder.from(
        fetcher = apiFetcher<UserSession, LoginResponse> { request -> retrofitApi.getUserSessionToken(request.login, request.token) },
        sourceOfTruth = SourceOfTruth.of(
            reader = { storage.loggedUser },
            writer = { _, newValue -> storage.updateLoggedUser(newValue.toLoggedUserInfo()) },
            delete = { storage.updateLoggedUser(null) },
            deleteAll = { storage.updateLoggedUser(null) },
        ),
    )
        .scope(appScopes.applicationScope)
        .build()
}

fun LoginResponse.toLoggedUserInfo() = LoggedUserInfo(
    id = profile.id,
    userToken = userkey,
    avatarUrl = profile.avatar,
    backgroundUrl = profile.background,
)
