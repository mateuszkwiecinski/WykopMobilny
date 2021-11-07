package io.github.wykopmobilny.ui.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SimpleViewStateStorage {

    private val _state = MutableStateFlow(SimpleViewState())
    val state: StateFlow<SimpleViewState> = _state

    fun update(updater: (old: SimpleViewState) -> SimpleViewState) {
        _state.update(updater)
    }

    data class SimpleViewState(
        val isLoading: Boolean = false,
        val failedAction: FailedAction? = null,
    )
}

data class FailedAction(
    val cause: Throwable,
    val retryAction: (() -> Unit)? = null,
)

sealed class ItemState {
    object InProgress : ItemState()
    data class Error(val error: Throwable) : ItemState()
}
