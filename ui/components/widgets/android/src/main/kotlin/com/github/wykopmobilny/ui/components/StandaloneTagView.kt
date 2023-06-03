package com.github.wykopmobilny.ui.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.scale
import androidx.core.text.underline
import com.github.wykopmobilny.ui.components.utils.readColorAttr
import io.github.wykopmobilny.ui.components.widgets.TagUi
import io.github.wykopmobilny.ui.components.widgets.android.databinding.ViewTagStandaloneBinding
import io.github.wykopmobilny.utils.bindings.setOnClick
import androidx.appcompat.R as AppcompatR

@Suppress("FunctionName")
fun StandaloneTagView(context: Context, model: TagUi): View = ViewTagStandaloneBinding.inflate(LayoutInflater.from(context)).run {
    root.renderTag(model)
    root
}

fun TextView.renderTag(model: TagUi) {
    text = buildSpannedString {
        scale(0.8f) {
            append("#")
        }
        color(context.readColorAttr(AppcompatR.attr.colorAccent).defaultColor) {
            underline {
                append(model.name)
            }
        }
    }
    setOnClick(model.onClick)
}
