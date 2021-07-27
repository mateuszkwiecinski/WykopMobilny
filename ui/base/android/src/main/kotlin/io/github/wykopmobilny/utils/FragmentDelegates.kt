package io.github.wykopmobilny.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun stringArgument(key: String) = object : ReadWriteProperty<Fragment, String> {

    override fun getValue(thisRef: Fragment, property: KProperty<*>) =
        thisRef.arguments?.getString(key, null).let(::checkNotNull)

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: String) {
        thisRef.arguments = (thisRef.arguments ?: Bundle())
            .apply { putString(key, value) }
    }
}
