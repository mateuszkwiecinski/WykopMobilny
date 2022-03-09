package com.github.wykopmobilny.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import com.github.wykopmobilny.ui.components.utils.readColorAttr
import io.github.wykopmobilny.ui.components.widgets.ColorConst
import io.github.wykopmobilny.ui.components.widgets.ColorReference
import io.github.wykopmobilny.ui.components.widgets.Color as DomainColor

fun DomainColor?.toColorInt(context: Context): ColorStateList =
    when (this) {
        is ColorConst -> ColorStateList.valueOf(Color.parseColor(hexValue))
        is ColorReference -> when (this) {
            ColorReference.Admin -> com.google.android.material.R.attr.colorOnSurface
        }.let(context::readColorAttr)
        null -> ColorStateList.valueOf(Color.TRANSPARENT)
    }
