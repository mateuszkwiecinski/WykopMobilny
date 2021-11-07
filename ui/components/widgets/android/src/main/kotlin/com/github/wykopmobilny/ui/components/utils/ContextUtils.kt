package com.github.wykopmobilny.ui.components.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.Dimension
import androidx.core.content.ContextCompat

@Dimension(unit = Dimension.PX)
fun Number.dpToPx(
    resources: Resources,
) = (toFloat() * resources.displayMetrics.density).toInt()

fun Context.readColorAttr(@AttrRes attrColor: Int): ColorStateList {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrColor, typedValue, true)

    return if (typedValue.resourceId > 0) {
        ContextCompat.getColorStateList(this, typedValue.resourceId)
            ?: ColorStateList.valueOf(ContextCompat.getColor(this, typedValue.resourceId))
    } else {
        ColorStateList.valueOf(typedValue.data)
    }
}


val View.layoutInflater
    get() = LayoutInflater.from(context)
