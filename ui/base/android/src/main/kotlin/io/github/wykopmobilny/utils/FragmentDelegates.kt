package io.github.wykopmobilny.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun stringArgument(key: String): ReadWriteProperty<Fragment, String> = ArgumentDelegate(key)

fun stringArgumentNullable(key: String): ReadWriteProperty<Fragment, String?> = ArgumentDelegate(key)

private class ArgumentDelegate<T : String?>(private val key: String) : ReadWriteProperty<Fragment, T> {

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Fragment, property: KProperty<*>) =
        thisRef.arguments?.getString(key, null) as T

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        thisRef.arguments = (thisRef.arguments ?: Bundle())
            .apply { putString(key, value) }
    }
}
