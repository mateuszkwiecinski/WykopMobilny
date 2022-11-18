package io.github.wykopmobilny.models.mapper.apiv2

import io.github.wykopmobilny.api.entries.FilteredData
import io.github.wykopmobilny.api.filters.OWMContentFilter
import io.github.wykopmobilny.api.responses.LinkResponse
import io.github.wykopmobilny.models.dataclass.Link
import io.github.wykopmobilny.utils.api.stripImageCompression
import io.github.wykopmobilny.utils.textview.removeHtml

fun LinkResponse.filterLink(owmContentFilter: OWMContentFilter) =
    owmContentFilter.filterLink(
        Link(
            id = id,
            title = title?.removeHtml().orEmpty(),
            description = description?.removeHtml().orEmpty(),
            tags = tags,
            sourceUrl = sourceUrl,
            voteCount = voteCount,
            buryCount = buryCount,
            comments = mutableListOf(),
            commentsCount = commentsCount,
            relatedCount = relatedCount,
            author = author.let(AuthorMapper::map),
            fullDate = date,
            previewImage = preview,
            fullImage = preview?.stripImageCompression(),
            plus18 = plus18,
            canVote = canVote,
            isHot = isHot,
            userVote = userVote,
            userFavorite = userFavorite ?: false,
            app = app,
            violationUrl = violationUrl,
            gotSelected = false,
            isBlocked = false,
        ),
    )

fun List<LinkResponse>.filterLinks(owmContentFilter: OWMContentFilter) =
    FilteredData(
        totalCount = size,
        filtered = map { response -> response.filterLink(owmContentFilter) },
    )
