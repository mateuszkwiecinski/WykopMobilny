package io.github.wykopmobilny.domain.profile.datasource

import com.dropbox.android.external.store4.SourceOfTruth
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.github.wykopmobilny.api.responses.AuthorResponse
import io.github.wykopmobilny.api.responses.EmbedResponse
import io.github.wykopmobilny.api.responses.EntryLinkResponse
import io.github.wykopmobilny.data.cache.api.AppCache
import io.github.wykopmobilny.data.cache.api.Embed
import io.github.wykopmobilny.data.cache.api.EmbedType
import io.github.wykopmobilny.data.cache.api.EntryEntity
import io.github.wykopmobilny.data.cache.api.LinkEntity
import io.github.wykopmobilny.data.cache.api.ProfileActionsEntity
import io.github.wykopmobilny.data.cache.api.ProfileQueries
import io.github.wykopmobilny.data.cache.api.UserVote
import io.github.wykopmobilny.domain.profile.EntryInfo
import io.github.wykopmobilny.domain.profile.LinkInfo
import io.github.wykopmobilny.domain.profile.ProfileAction
import io.github.wykopmobilny.domain.profile.UserInfo
import io.github.wykopmobilny.domain.profile.di.toColorEntity
import io.github.wykopmobilny.domain.profile.di.toGenderEntity
import io.github.wykopmobilny.domain.profile.toColorDomain
import io.github.wykopmobilny.domain.profile.toGenderDomain
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

internal fun profileSourceOfTruth(
    profileId: String,
    cache: AppCache,
) = SourceOfTruth.of<Int, List<EntryLinkResponse>, List<ProfileAction>>(
    reader = { page ->
        val linksStream = cache.profileActionsQueries.selectLinksPage(profileId = profileId, page = page)
            .asFlow()
            .mapToList(AppDispatchers.IO)
            .map { links ->
                links.map { link ->
                    val entry = LinkInfo(
                        id = link.id,
                        title = link.title,
                        description = link.description,
                        tags = link.tags.split(" "),
                        sourceUrl = link.sourceUrl,
                        previewImageUrl = link.previewImageUrl,
                        author = UserInfo(
                            profileId = link.profileId,
                            avatarUrl = link.avatar,
                            rank = link.rank,
                            gender = link.gender?.toGenderDomain(),
                            color = link.color.toColorDomain(),
                        ),
                        commentsCount = link.commentsCount,
                        voteCount = link.voteCount,
                        relatedCount = link.relatedCount,
                        postedAt = link.postedAt,
                        app = link.app,
                        userAction = link.userVote,
                        userFavorite = link.userFavorite,
                    )
                    entry to link.orderOnPage
                }
            }

        val entriesStream = cache.profileActionsQueries.selectEntriesPage(profileId = profileId, page = page)
            .asFlow()
            .mapToList(AppDispatchers.IO)
            .map { entities ->
                entities.map { action ->
                    val entry = EntryInfo(
                        id = action.id,
                        postedAt = action.postedAt,
                        body = action.body,
                        voteCount = action.voteCount,
                        previewImageUrl = action.preview,
                        commentsCount = action.commentsCount,
                        author = UserInfo(
                            profileId = action.profileId,
                            avatarUrl = action.avatar,
                            rank = action.rank,
                            gender = action.gender?.toGenderDomain(),
                            color = action.color.toColorDomain(),
                        ),
                        app = action.app,
                        userAction = action.userVote,
                        isFavorite = action.isFavorite,
                    )
                    entry to action.orderOnPage
                }
            }
        combine(
            linksStream,
            entriesStream,
        ) { links, entries ->
            val all = links + entries
            all.sortedBy { it.second }.map { it.first }
        }
    },
    writer = { page, pageData ->
        cache.profileActionsQueries.transaction {
            cache.profileActionsQueries.deletePage(profileId, page)
            pageData.forEachIndexed { index, action ->
                action.link?.run {
                    cache.profileQueries.upsert(
                        id = author.login,
                        avatar = author.avatar,
                        color = author.color.toColorEntity(),
                        gender = author.sex.toGenderEntity(),
                    )
                    cache.linksQueries.insertOrReplace(
                        LinkEntity(
                            id = id,
                            title = title.orEmpty(),
                            description = description.orEmpty(),
                            tags = tags,
                            sourceUrl = sourceUrl,
                            previewImageUrl = preview,
                            voteCount = voteCount,
                            buryCount = buryCount,
                            commentsCount = commentsCount,
                            relatedCount = relatedCount,
                            postedAt = date,
                            plus18 = plus18,
                            canVote = canVote,
                            isHot = isHot,
                            userVote = userVote.asUserVote(),
                            userFavorite = userFavorite == true,
                            userObserve = userObserve == true,
                            app = app,
                            profileId = author.login,
                        ),
                    )
                }
                action.entry?.run {
                    cache.profileQueries.upsert(author)
                    embed?.toEntity()?.let(cache.embedQueries::insertOrReplace)

                    cache.entriesQueries.insertOrReplace(
                        EntryEntity(
                            id = id,
                            postedAt = date,
                            body = body.orEmpty(),
                            voteCount = voteCount,
                            embedId = embed?.url,
                            commentsCount = commentsCount,
                            isBlocked = blocked,
                            isFavorite = favorite,
                            app = app,
                            canComment = isCommentingPossible == true,
                            violationUrl = violationUrl,
                            userVote = userVote.asUserVote(),
                            profileId = author.login,
                        ),
                    )
                }
                cache.profileActionsQueries.insertPage(
                    ProfileActionsEntity(
                        profileId = profileId,
                        linkId = action.link?.id,
                        entryId = action.entry?.id,
                        page = page,
                        orderOnPage = index,
                    ),
                )
            }
        }
    },
    delete = { page -> cache.profileActionsQueries.deletePage(profileId, page = page) },
    deleteAll = { cache.profileActionsQueries.deleteByProfile(profileId) },
)

internal fun ProfileQueries.upsert(author: AuthorResponse) {
    upsert(
        id = author.login,
        avatar = author.avatar,
        color = author.color.toColorEntity(),
        gender = author.sex.toGenderEntity(),
    )
}

internal fun EmbedResponse.toEntity(): Embed {
    val knownType = when (type) {
        "image" -> if (animated) {
            EmbedType.StaticImage
        } else {
            EmbedType.AnimatedImage
        }
        "video" -> EmbedType.Video
        else -> EmbedType.Unknown
    }

    return Embed(
        id = url,
        type = knownType,
        fileName = source,
        preview = preview,
        size = size,
        hasAdultContent = plus18,
    )
}

internal fun String?.asUserVote() =
    when (this) {
        "dig" -> UserVote.Down
        "bury" -> UserVote.Up
        else -> null
    }

internal fun Int.asUserVote() =
    when {
        this > 0 -> UserVote.Up
        this < 0 -> UserVote.Down
        else -> null
    }
