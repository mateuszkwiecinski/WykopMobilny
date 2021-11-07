package io.github.wykopmobilny.utils.bindings

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import io.github.wykopmobilny.ui.base.components.Drawable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

suspend fun Flow<() -> Unit>.setOnClick(view: View) {
    collect { onClick -> view.setOnClick(onClick) }
}

fun View.setOnClick(onClicked: (() -> Unit)?) {
    if (onClicked == null) {
        setOnClickListener(null)
        isClickable = false
    } else {
        setOnClickListener { onClicked() }
    }
}

fun View.setOnLongClick(callback: (() -> Unit)?) {
    if (callback == null) {
        setOnLongClickListener(null)
        isLongClickable = false
    } else {
        setOnLongClickListener { callback(); true }
    }
}

fun ImageView.setImage(icon: Drawable?) {
    isVisible = icon != null
    icon?.drawableRes?.let(::setImageResource) ?: setImageDrawable(null)
}
