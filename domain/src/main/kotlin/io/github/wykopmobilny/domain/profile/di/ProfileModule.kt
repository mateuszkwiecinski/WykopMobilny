package io.github.wykopmobilny.domain.profile.di

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.github.wykopmobilny.api.endpoints.ProfileRetrofitApi
import io.github.wykopmobilny.api.responses.ProfileResponse
import io.github.wykopmobilny.api.responses.WykopApiResponse
import io.github.wykopmobilny.data.cache.api.AppCache
import io.github.wykopmobilny.data.cache.api.GenderEntity
import io.github.wykopmobilny.data.cache.api.ProfileDetailsView
import io.github.wykopmobilny.data.cache.api.ProfileEntity
import io.github.wykopmobilny.data.cache.api.UserColorEntity
import io.github.wykopmobilny.domain.api.PagingSource
import io.github.wykopmobilny.domain.api.StoreMediator
import io.github.wykopmobilny.domain.api.apiFetcher
import io.github.wykopmobilny.domain.di.ScopeInitializer
import io.github.wykopmobilny.domain.profile.GetProfileActionsQuery
import io.github.wykopmobilny.domain.profile.GetProfileDetailsQuery
import io.github.wykopmobilny.domain.profile.InitializeProfile
import io.github.wykopmobilny.domain.profile.ProfileAction
import io.github.wykopmobilny.domain.profile.ProfileId
import io.github.wykopmobilny.domain.profile.datasource.profileSourceOfTruth
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.base.SimpleViewStateStorage
import io.github.wykopmobilny.ui.profile.GetProfileActions
import io.github.wykopmobilny.ui.profile.GetProfileDetails
import javax.inject.Provider

@Module
internal abstract class ProfileModule {

    companion object {

        @ProfileScope
        @Provides
        fun viewState() = SimpleViewStateStorage()

        @ProfileScope
        @Provides
        fun actionsStore(
            @ProfileId profileId: String,
            retrofitApi: ProfileRetrofitApi,
            appScopes: AppScopes,
            cache: AppCache,
        ): Store<Int, List<ProfileAction>> = StoreBuilder.from(
            fetcher = apiFetcher { page ->
                // pagination is broken on the  api side
                if (page == 1) {
                    retrofitApi.getActions(profileId)
                } else {
                    WykopApiResponse(data = emptyList(), error = null)
                }
            },
            sourceOfTruth = profileSourceOfTruth(profileId, cache),
        )
            .scope(appScopes.applicationScope)
            .build()

        @Provides
        fun actionsPager(
            mediator: StoreMediator<ProfileAction>,
            pagingSource: Provider<PagingSource<ProfileAction>>,
        ): Pager<Int, ProfileAction> = Pager(
            config = PagingConfig(pageSize = 20),
            remoteMediator = mediator,
            pagingSourceFactory = pagingSource::get,
        )

        @ProfileScope
        @Provides
        fun profileStore(
            @ProfileId profileId: String,
            retrofitApi: ProfileRetrofitApi,
            appScopes: AppScopes,
            cache: AppCache,
        ): Store<Unit, ProfileDetailsView> = StoreBuilder.from(
            fetcher = apiFetcher { retrofitApi.getIndex(profileId) },
            sourceOfTruth = SourceOfTruth.of<Unit, ProfileResponse, ProfileDetailsView>(
                reader = {
                    cache.profileQueries.selectById(profileId)
                        .asFlow()
                        .mapToOneOrNull(AppDispatchers.IO)
                },
                writer = { _, input ->
                    cache.transaction {
                        if (input.isBlocked == true) {
                            cache.profileStateQueries.blockProfile(input.id)
                        } else {
                            cache.profileStateQueries.unblockProfile(input.id)
                        }
                        if (input.isObserved == true) {
                            cache.profileStateQueries.observeProfile(input.id)
                        } else {
                            cache.profileStateQueries.unobserveProfile(input.id)
                        }
                        cache.profileQueries.insertOrReplace(input.toProfileEntity())
                    }
                },
                delete = { cache.profileQueries.deleteById(profileId) },
                deleteAll = { cache.profileQueries.deleteAll() },
            ),
        )
            .scope(appScopes.applicationScope)
            .build()
    }

    @Binds
    abstract fun GetProfileDetailsQuery.getProfileDetails(): GetProfileDetails

    @Binds
    abstract fun GetProfileActionsQuery.getProfileEntries(): GetProfileActions

    @Binds
    abstract fun InitializeProfile.scopeInitializer(): ScopeInitializer
}

private fun ProfileResponse.toProfileEntity() = ProfileEntity(
    id = id,
    signupAt = signupAt,
    background = background,
    isVerified = isVerified == true,
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
    color = color.toColorEntity(),
    gender = sex?.toGenderEntity(),
    avatar = avatar,
)

@Suppress("MagicNumber")
internal fun Int.toColorEntity() =
    when (this) {
        0 -> UserColorEntity.Green
        1 -> UserColorEntity.Orange
        2 -> UserColorEntity.Claret
        5 -> UserColorEntity.Admin
        1001 -> UserColorEntity.Banned
        1002 -> UserColorEntity.Deleted
        2001 -> UserColorEntity.Client
        else -> UserColorEntity.Unknown
    }

internal fun String.toGenderEntity() =
    when (this) {
        "male" -> GenderEntity.Male
        "female" -> GenderEntity.Female
        else -> null
    }
