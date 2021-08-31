package io.github.wykopmobilny.storage.android

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.Executor
import kotlin.reflect.KProperty

@Suppress("UnnecessaryAbstractClass")
internal abstract class BasePreferences(
    private val context: Context,
    executor: Executor,
) {

    private val coroutineScope = CoroutineScope(Job() + executor.asCoroutineDispatcher())

    protected val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
    }
    protected val preferences = callbackFlow<String?> {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key -> trySend(key) }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
        .shareIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
            replay = 1,
        )

    abstract class PrefDelegate<T>(val key: String) {
        abstract operator fun getValue(thisRef: Any?, property: KProperty<*>): T
        abstract operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
    }

    fun stringPref(key: String) = StringPrefDelegate(key)

    inner class StringPrefDelegate(prefKey: String) : PrefDelegate<String?>(prefKey) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): String? = prefs.getString(key, null)
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
            prefs.edit().putString(key, value).apply()
        }
    }
}
