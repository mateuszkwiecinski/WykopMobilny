package io.github.wykopmobilny.domain.di

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import dagger.Module
import dagger.Provides
import io.github.wykopmobilny.api.endpoints.ProfileRetrofitApi
import io.github.wykopmobilny.data.cache.api.AppCache
import io.github.wykopmobilny.data.cache.api.ProfileEntity
import io.github.wykopmobilny.domain.api.apiCall
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.AppScopes
import javax.inject.Singleton

@Module
internal class ProfileStores {

    @Singleton
    @Provides
    fun profileStore(
        retrofitApi: ProfileRetrofitApi,
        appScopes: AppScopes,
        cache: AppCache,
    ) = StoreBuilder.from<String, ProfileEntity, ProfileEntity>(
        fetcher = Fetcher.ofResult { username ->
            apiCall(
                rawCall = { retrofitApi.getIndex(username) },
                mapping = {
                    ProfileEntity(
                        id = id,
                        signupAt = signupAt,
                        background = background,
                        isVerified = isVerified == true,
                        isObserved = isObserved == true,
                        isBlocked = isBlocked == true,
                        email = email,
                        description = description,
                        name = name,
                        wwwUrl = wwwUrl,
                        jabberUrl = jabberUrl,
                        ggUrl = ggUrl,
                        city = city,
                        facebookUrl = facebookUrl,
                        twitterUrl = twitterUrl,
                        instagramUrl = instagramUrl,
                        linksAddedCount = linksAddedCount,
                        linksPublishedCount = linksPublishedCount,
                        commentsCount = commentsCount,
                        rank = rank,
                        followers = followers,
                        following = following,
                        entriesCount = entriesCount,
                        entriesCommentsCount = entriesCommentsCount,
                        diggsCount = diggsCount,
                        buriesCount = buriesCount,
                        violationUrl = violationUrl,
                        banReason = ban?.reason,
                        banDate = ban?.date,
                        color = color,
                        sex = sex,
                        avatar = avatar,
                    )
                },
            )
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key ->
                cache.profilesQueries.selectById(key)
                    .asFlow()
                    .mapToOneOrNull(AppDispatchers.Default)
            },
            writer = { _, input -> cache.profilesQueries.insertOrReplace(input) },
            delete = { key -> cache.profilesQueries.deleteById(key) },
            deleteAll = { cache.profilesQueries.deleteAll() },
        ),
    )
        .scope(appScopes.applicationScope)
        .build()
}
