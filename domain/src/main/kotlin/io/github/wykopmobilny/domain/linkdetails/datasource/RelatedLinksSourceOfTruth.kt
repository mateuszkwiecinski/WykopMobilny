package io.github.wykopmobilny.domain.linkdetails.datasource

import com.dropbox.android.external.store4.SourceOfTruth
import io.github.wykopmobilny.api.responses.RelatedResponse
import io.github.wykopmobilny.data.cache.api.AppCache
import io.github.wykopmobilny.domain.linkdetails.RelatedLink
import kotlinx.coroutines.flow.flow

internal fun relatedLinksSourceOfTruth(
    cache: AppCache,
) = SourceOfTruth.of<Long, List<RelatedResponse>, List<RelatedLink>>(
    reader = { key ->
        flow<List<RelatedLink>> { }
    },
    writer = { key, list ->
    },
    delete = { key -> },
    deleteAll = {
    },
)
