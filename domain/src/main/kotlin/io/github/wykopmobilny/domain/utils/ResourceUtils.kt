package io.github.wykopmobilny.domain.utils

import io.github.wykopmobilny.ui.base.FailedAction
import io.github.wykopmobilny.ui.base.Resource
import kotlinx.coroutines.CoroutineScope

internal suspend fun <T> withResource(
    refresh: suspend () -> T,
    update: (Resource) -> Unit,
    launch: (suspend CoroutineScope.() -> Unit) -> Unit? = { _ -> null },
): Result<T> {
    update(Resource.loading())
    return runCatching { refresh() }
        .onSuccess { update(Resource.idle()) }
        .onFailure { failure ->
            update(
                Resource.error(
                    FailedAction(
                        cause = failure,
                        retryAction = { launch { withResource(refresh, update) } },
                    ),
                ),
            )
        }
}
