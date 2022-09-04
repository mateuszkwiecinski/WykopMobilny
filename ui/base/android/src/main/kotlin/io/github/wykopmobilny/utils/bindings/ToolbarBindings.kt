package io.github.wykopmobilny.utils.bindings

import androidx.activity.ComponentActivity
import androidx.appcompat.widget.Toolbar

fun Toolbar.bindBackButton(activity: ComponentActivity?) {
    setNavigationOnClickListener { activity?.onBackPressedDispatcher?.onBackPressed() }
}
