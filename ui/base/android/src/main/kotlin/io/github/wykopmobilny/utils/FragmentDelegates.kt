package io.github.wykopmobilny.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun stringArgument(key: String): ReadWriteProperty<Fragment, String> = StringArgumentDelegate(key)

fun longArgument(key: String): ReadWriteProperty<Fragment, Long> = LongArgumentDelegate(key)

fun stringArgumentNullable(key: String): ReadWriteProperty<Fragment, String?> = StringArgumentDelegate(key)

fun longArgumentNullable(key: String): ReadWriteProperty<Fragment, Long?> = LongArgumentDelegate(key)

private class StringArgumentDelegate<T : String?>(private val key: String) : ReadWriteProperty<Fragment, T> {

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Fragment, property: KProperty<*>) =
        thisRef.arguments?.getString(key, null) as T

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        thisRef.arguments = (thisRef.arguments ?: Bundle())
            .apply { putString(key, value) }
    }
}

private class LongArgumentDelegate<T : Long?>(private val key: String) : ReadWriteProperty<Fragment, T> {

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Fragment, property: KProperty<*>) =
        thisRef.arguments?.run {
            if (containsKey(key)) {
                getLong(key, 0L)
            } else {
                null
            }
        } as T

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        thisRef.arguments = (thisRef.arguments ?: Bundle())
            .apply {
                if (value == null) {
                    remove(key)
                } else {
                    putLong(key, value)
                }
            }
    }
}
