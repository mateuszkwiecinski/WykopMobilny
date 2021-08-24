package io.github.wykopmobilny.domain.utils

import io.github.aakira.napier.Napier
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.base.launchIn
import kotlinx.coroutines.CoroutineScope

internal inline fun <reified T : Any> AppScopes.safe(crossinline block: suspend CoroutineScope.() -> Unit) {
    launchIn<T> {
        runCatching { block() }
            .onFailure { Napier.e("Something wasn't safe 😬", it) }
    }
}
