package io.github.wykopmobilny.ui.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SimpleViewStateStorage {

    private val _state = MutableStateFlow(Resource.idle())
    val state: StateFlow<Resource> = _state

    fun update(updater: (old: Resource) -> Resource) {
        _state.update(updater)
    }
}

@Suppress("DataClassPrivateConstructor")
data class Resource private constructor(
    val isLoading: Boolean = false,
    val failedAction: FailedAction? = null,
) {

    companion object {

        fun loading() = Resource(
            isLoading = true,
            failedAction = null,
        )

        fun error(failedAction: FailedAction) = Resource(
            isLoading = false,
            failedAction = failedAction,
        )

        fun idle() = Resource(
            isLoading = false,
            failedAction = null,
        )
    }
}

data class FailedAction(
    val cause: Throwable,
    val retryAction: (() -> Unit)? = null,
)

sealed class ItemState {
    object InProgress : ItemState()
    data class Error(val error: Throwable) : ItemState()
}
