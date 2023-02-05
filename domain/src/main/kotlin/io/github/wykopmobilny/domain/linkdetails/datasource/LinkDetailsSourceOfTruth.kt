package io.github.wykopmobilny.domain.linkdetails.datasource

import com.dropbox.android.external.store4.SourceOfTruth
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import io.github.wykopmobilny.api.responses.LinkResponse
import io.github.wykopmobilny.data.cache.api.AppCache
import io.github.wykopmobilny.data.cache.api.LinkEntity
import io.github.wykopmobilny.domain.profile.LinkInfo
import io.github.wykopmobilny.domain.profile.UserInfo
import io.github.wykopmobilny.domain.profile.datasource.asUserVote
import io.github.wykopmobilny.domain.profile.datasource.upsert
import io.github.wykopmobilny.domain.profile.toColorDomain
import io.github.wykopmobilny.domain.profile.toGenderDomain
import io.github.wykopmobilny.kotlin.AppDispatchers
import kotlinx.coroutines.flow.map

internal fun linkDetailsSourceOfTruth(
    cache: AppCache,
) = SourceOfTruth.of<Long, LinkResponse, LinkInfo>(
    reader = { linkId ->
        cache.linksQueries.selectById(id = linkId)
            .asFlow()
            .mapToOneOrNull(AppDispatchers.IO)
            .map { link ->
                link ?: return@map null
                LinkInfo(
                    id = link.id,
                    title = link.title,
                    isHot = link.isHot,
                    description = link.description,
                    tags = link.tags.split(" ").map { it.removePrefix("#") },
                    sourceUrl = link.sourceUrl,
                    previewImageUrl = link.previewImageUrl,
                    fullImageUrl = link.fullImageUrl,
                    author = UserInfo(
                        profileId = link.profileId,
                        avatarUrl = link.avatar,
                        rank = link.rank,
                        gender = link.gender?.toGenderDomain(),
                        color = link.color.toColorDomain(),
                    ),
                    commentsCount = link.commentsCount,
                    voteCount = link.voteCount,
                    buryCount = link.buryCount,
                    relatedCount = link.relatedCount,
                    postedAt = link.postedAt,
                    app = link.app,
                    userAction = link.userVote,
                    userFavorite = link.userFavorite,
                )
            }
    },
    writer = { _, link ->
        cache.transaction {
            cache.profileQueries.upsert(link.author)
            cache.linksQueries.insertOrReplace(
                LinkEntity(
                    id = link.id,
                    title = link.title.orEmpty(),
                    description = link.description.orEmpty(),
                    tags = link.tags,
                    sourceUrl = link.sourceUrl,
                    previewImageUrl = link.preview?.stripImageCompression(),
                    fullImageUrl = link.preview?.stripImageCompression(),
                    voteCount = link.voteCount,
                    buryCount = link.buryCount,
                    commentsCount = link.commentsCount,
                    relatedCount = link.relatedCount,
                    postedAt = link.date,
                    plus18 = link.plus18,
                    canVote = link.canVote,
                    isHot = link.isHot,
                    userVote = link.userVote.asUserVote(),
                    userFavorite = link.userFavorite == true,
                    userObserve = link.userObserve == true,
                    app = link.app,
                    profileId = link.author.login,
                ),
            )
        }
    },
    delete = { linkId -> cache.linksQueries.deleteById(linkId) },
)

internal fun String.stripImageCompression(): String {
    val extension = substringAfterLast(".")
    val baseUrl = substringBeforeLast(",")
    return baseUrl + if (!baseUrl.endsWith(extension)) ".$extension" else ""
}
