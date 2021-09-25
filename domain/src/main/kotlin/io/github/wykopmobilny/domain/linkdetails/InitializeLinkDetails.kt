package io.github.wykopmobilny.domain.linkdetails

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.fresh
import io.github.wykopmobilny.domain.di.ScopeInitializer
import io.github.wykopmobilny.domain.linkdetails.di.LinkId
import io.github.wykopmobilny.domain.profile.LinkInfo
import io.github.wykopmobilny.ui.base.FailedAction
import io.github.wykopmobilny.ui.base.SimpleViewStateStorage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class InitializeLinkDetails @Inject constructor(
    @LinkId private val linkId: Long,
    private val linkStore: Store<Long, LinkInfo>,
    private val commentsStore: Store<Long, Map<LinkComment, List<LinkComment>>>,
    private val viewStateStorage: SimpleViewStateStorage,
) : ScopeInitializer {

    override suspend fun initialize() {
        viewStateStorage.update { it.copy(isLoading = true, failedAction = null) }
        runCatching {
            coroutineScope {
                launch { commentsStore.fresh(key = linkId) }
                launch { linkStore.fresh(key = linkId) }
            }
        }
            .onFailure { failure -> viewStateStorage.update { it.copy(isLoading = false, failedAction = FailedAction(failure)) } }
            .onSuccess { viewStateStorage.update { it.copy(isLoading = false) } }
    }
}
