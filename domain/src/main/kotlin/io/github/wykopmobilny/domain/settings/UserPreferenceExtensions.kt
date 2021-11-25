package io.github.wykopmobilny.domain.settings

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.data.storage.api.PreferenceEntity
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

internal fun <T> AppStorage.get(key: UserSetting<T>): Flow<T?> =
    preferencesQueries.getPreference(key.preferencesKey).asFlow()
        .mapToOneOrNull(AppDispatchers.IO)
        .map { value -> value?.let(key.mapping) }
        .distinctUntilChanged()

internal suspend fun <T> AppStorage.update(key: UserSetting<T>, value: T?) = withContext(AppDispatchers.IO) {
    val mapped = value?.let(key.reverseMapping)
    if (mapped == null) {
        preferencesQueries.delete(key.preferencesKey)
    } else {
        preferencesQueries.insertOrReplace(PreferenceEntity(key = key.preferencesKey, mapped))
    }
}

internal class UserSetting<T>(
    val preferencesKey: String,
    val mapping: (String) -> T?,
    val reverseMapping: (T) -> String?,
)

internal fun <T : Enum<T>> enumMapping(
    preferencesKey: String,
    enumMapping: Map<T, String>,
) = UserSetting(
    preferencesKey = preferencesKey,
    mapping = { enumMapping.entries.firstOrNull { (_, value) -> value == it }?.key },
    reverseMapping = { enumMapping[it] },
)

internal fun booleanMapping(
    preferencesKey: String,
) = UserSetting(
    preferencesKey = preferencesKey,
    mapping = { it.toBoolean() },
    reverseMapping = { it.toString() },
)

internal fun durationMapping(
    preferencesKey: String,
) = UserSetting(
    preferencesKey = preferencesKey,
    mapping = { it.toLongOrNull()?.milliseconds },
    reverseMapping = { it.inWholeMilliseconds.toString() },
)

internal fun longMapping(
    preferencesKey: String,
) = UserSetting(
    preferencesKey = preferencesKey,
    mapping = { it.toLongOrNull() },
    reverseMapping = { it.toString() },
)
