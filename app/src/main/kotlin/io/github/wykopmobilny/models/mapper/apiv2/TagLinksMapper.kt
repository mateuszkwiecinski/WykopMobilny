package io.github.wykopmobilny.models.mapper.apiv2

import io.github.wykopmobilny.api.filters.OWMContentFilter
import io.github.wykopmobilny.api.responses.TagLinksResponse
import io.github.wykopmobilny.models.dataclass.TagLinks

object TagLinksMapper {

    fun map(value: TagLinksResponse, owmContentFilter: OWMContentFilter) = TagLinks(
        entries = value.data.orEmpty().map { it.filterLink(owmContentFilter = owmContentFilter) },
        meta = value.meta,
    )
}
