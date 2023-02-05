package io.github.wykopmobilny.domain.utils

import io.github.aakira.napier.Napier
import io.github.wykopmobilny.kotlin.AppScopes
import io.github.wykopmobilny.kotlin.launchIn
import io.github.wykopmobilny.kotlin.launchInKeyed
import kotlinx.coroutines.CoroutineScope

internal inline fun <reified T : Any> AppScopes.safe(crossinline block: suspend CoroutineScope.() -> Unit) {
    launchIn<T> {
        runCatching { block() }
            .onFailure { Napier.e("Something wasn't safe 😬", it) }
    }
}
internal inline fun <reified T : Any> AppScopes.safeKeyed(id: Any, crossinline block: suspend CoroutineScope.() -> Unit) {
    launchInKeyed<T>(id) {
        runCatching { block() }
            .onFailure { Napier.e("Something wasn't safe 😬", it) }
    }
}
