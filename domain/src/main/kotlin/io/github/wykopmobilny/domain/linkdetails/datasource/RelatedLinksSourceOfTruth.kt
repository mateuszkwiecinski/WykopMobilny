package io.github.wykopmobilny.domain.linkdetails.datasource

import com.dropbox.android.external.store4.SourceOfTruth
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.github.wykopmobilny.api.responses.RelatedResponse
import io.github.wykopmobilny.data.cache.api.AppCache
import io.github.wykopmobilny.data.cache.api.RelatedLinkEntity
import io.github.wykopmobilny.data.cache.api.linksRelated.SelectByLinkId
import io.github.wykopmobilny.domain.linkdetails.RelatedLink
import io.github.wykopmobilny.domain.profile.UserInfo
import io.github.wykopmobilny.domain.profile.datasource.asUserVote
import io.github.wykopmobilny.domain.profile.datasource.upsert
import io.github.wykopmobilny.domain.profile.toColorDomain
import io.github.wykopmobilny.domain.profile.toGenderDomain
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.flow.map

internal fun relatedLinksSourceOfTruth(
    cache: AppCache,
) = SourceOfTruth.of<Long, List<RelatedResponse>, List<RelatedLink>>(
    reader = { linkId ->
        cache.linksRelatedQueries.selectByLinkId(linkId = linkId)
            .asFlow()
            .mapToList(AppDispatchers.IO)
            .map { relatedLinks -> relatedLinks.map(SelectByLinkId::toDomain) }
    },
    writer = { linkId, links ->
        cache.transaction {
            links.forEachIndexed { index, link ->
                link.author?.let(cache.profileQueries::upsert)
                cache.linksRelatedQueries.insertOrReplace(link.toEntity(orderOnPage = index, linkId = linkId))
            }
        }
    },
    delete = { linkId -> cache.linksRelatedQueries.deleteByLinkId(linkId) },
)

private fun RelatedResponse.toEntity(
    orderOnPage: Int,
    linkId: Long,
) = RelatedLinkEntity(
    id = id,
    userVote = userVote?.asUserVote(),
    voteCount = voteCount,
    url = url,
    profileId = author?.login,
    title = title,
    linkId = linkId,
    orderOnPage = orderOnPage,
)

private fun SelectByLinkId.toDomain() = RelatedLink(
    id = id,
    url = url,
    voteCount = voteCount,
    author = profileId?.let {
        UserInfo(
            profileId = it,
            avatarUrl = avatar!!,
            rank = rank,
            gender = gender?.toGenderDomain(),
            color = color!!.toColorDomain(),
        )
    },
    title = title,
    userVote = userVote,
)
