package io.github.wykopmobilny.storage.android

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.Reusable
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.storage.api.LoggedUserInfo
import io.github.wykopmobilny.storage.api.SessionStorage
import io.github.wykopmobilny.storage.api.UserInfoStorage
import io.github.wykopmobilny.storage.api.UserSession
import io.github.wykopmobilny.ui.base.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@Reusable
internal class CredentialsPreferences @Inject constructor(
    private val context: Context,
) : SessionStorage, UserInfoStorage {

    private val login = stringPreferencesKey(name = "login")
    private val userKey = stringPreferencesKey(name = "userKey")
    private val userToken = stringPreferencesKey(name = "userToken")
    private val avatarUrl = stringPreferencesKey(name = "avatarUrl")
    private val backgroundUrl = stringPreferencesKey(name = "backgroundUrl")

    override val session =
        context.dataStore.data
            .map { prefs ->
                UserSession(
                    login = prefs[login] ?: return@map null,
                    token = prefs[userKey] ?: return@map null,
                )
            }
            .catch { Napier.e("Exception when reading user session", it) }
            .distinctUntilChanged()

    override suspend fun updateSession(value: UserSession?) {
        context.dataStore.edit { prefs ->
            if (value == null) {
                prefs -= login
                prefs -= userKey
            } else {
                prefs[login] = value.login
                prefs[userKey] = value.token
            }
        }
    }

    override val loggedUser =
        context.dataStore.data.map { prefs ->
            LoggedUserInfo(
                id = prefs[login] ?: return@map null,
                userToken = prefs[userToken] ?: return@map null,
                avatarUrl = prefs[avatarUrl] ?: return@map null,
                backgroundUrl = prefs[backgroundUrl],
            )
        }
            .catch { Napier.e("Exception when reading logged user", it) }
            .distinctUntilChanged()

    override suspend fun updateLoggedUser(value: LoggedUserInfo?) {
        context.dataStore.edit { prefs ->
            if (value == null) {
                prefs -= login
                prefs -= userToken
                prefs -= avatarUrl
                prefs -= backgroundUrl
            } else {
                prefs[login] = value.id
                prefs[userToken] = value.userToken
                prefs[avatarUrl] = value.avatarUrl
                value.backgroundUrl?.let { prefs[backgroundUrl] = it } ?: prefs.remove(backgroundUrl)
            }
        }
    }
}

internal val Context.dataStore by preferencesDataStore(
    name = "user_settings",
    scope = CoroutineScope(AppDispatchers.IO + SupervisorJob()),
    produceMigrations = {
        listOf(
            SharedPreferencesMigration(
                context = it,
                sharedPreferencesName = "Preferences",
            ),
        )
    },
)
