package io.github.wykopmobilny.domain.linkdetails

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.fresh
import io.github.wykopmobilny.domain.di.ScopeInitializer
import io.github.wykopmobilny.domain.linkdetails.di.LinkDetailsScope
import io.github.wykopmobilny.domain.profile.LinkInfo
import io.github.wykopmobilny.domain.utils.safeKeyed
import io.github.wykopmobilny.domain.utils.withResource
import io.github.wykopmobilny.links.details.LinkDetailsKey
import io.github.wykopmobilny.ui.base.AppScopes
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class InitializeLinkDetails @Inject constructor(
    private val key: LinkDetailsKey,
    private val linkStore: Store<Long, LinkInfo>,
    private val relatedLinksStore: Store<Long, List<RelatedLink>>,
    private val commentsStore: Store<Long, Map<LinkComment, List<LinkComment>>>,
    private val viewStateStorage: LinkDetailsViewStateStorage,
    private val appScopes: AppScopes,
) : ScopeInitializer {

    override suspend fun initialize() {
        val link = withResource(
            refresh = {
                coroutineScope {
                    launch { commentsStore.fresh(key = key.linkId) }
                    linkStore.fresh(key = key.linkId)
                }
            },
            update = { resource -> viewStateStorage.update { it.copy(generalResource = resource) } },
        )

        if (link.isSuccess && link.getOrThrow().relatedCount > 0) {
            withResource(
                refresh = { relatedLinksStore.fresh(key = key.linkId) },
                update = { resource -> viewStateStorage.update { it.copy(relatedResource = resource) } },
                launch = { callback -> appScopes.safeKeyed<LinkDetailsScope>(key, block = callback) },
            )
        }
    }
}
