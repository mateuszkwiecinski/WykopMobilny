package io.github.wykopmobilny.domain.blacklist

import io.github.wykopmobilny.domain.blacklist.di.BlacklistScope
import io.github.wykopmobilny.ui.base.ItemState
import io.github.wykopmobilny.ui.base.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@BlacklistScope
internal class BlacklistViewStateStorage @Inject constructor() {

    val state = MutableStateFlow(value = BlacklistViewState())

    fun update(updater: (BlacklistViewState) -> BlacklistViewState) {
        state.update(updater)
    }
}

data class BlacklistViewState(
    val generalResource: Resource = Resource.idle(),
    val tagsState: Map<String, ItemState> = emptyMap(),
    val usersState: Map<String, ItemState> = emptyMap(),
    val unblockConfirmation: List<String> = emptyList(),
)
