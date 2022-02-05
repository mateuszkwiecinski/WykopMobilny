package com.github.wykopmobilny.ui.components.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.AnyRes
import androidx.annotation.AttrRes
import androidx.annotation.Dimension
import androidx.core.content.ContextCompat
import io.github.aakira.napier.Napier

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

@Dimension(unit = Dimension.PX)
fun Context.readDimensionAttr(@AttrRes dimensionAttr: Int): Int {
    val dimensionValue = TypedValue()
    theme.resolveAttribute(dimensionAttr, dimensionValue, true)

    return if (dimensionValue.type != TypedValue.TYPE_DIMENSION) {
        Napier.w("Invalid dimension resource $dimensionAttr", Throwable("generate_stacktrace"))
        0
    } else {
        dimensionValue.getDimension(resources.displayMetrics).toInt()
    }
}

@AnyRes
fun Context.readAttr(@AttrRes attrColor: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrColor, typedValue, true)

    return typedValue.resourceId
}

val View.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(context)
