package com.github.wykopmobilny.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.view.updatePadding
import com.github.wykopmobilny.ui.components.utils.dpToPx
import com.github.wykopmobilny.ui.components.utils.readAttr
import com.github.wykopmobilny.ui.components.utils.readColorAttr
import io.github.wykopmobilny.ui.components.widgets.Button
import io.github.wykopmobilny.ui.components.widgets.android.R
import io.github.wykopmobilny.ui.components.widgets.android.databinding.ViewSimpleCounterBinding
import io.github.wykopmobilny.utils.bindings.setImage
import io.github.wykopmobilny.utils.bindings.setOnClick
import io.github.wykopmobilny.utils.bindings.toColorInt


class SimpleCounter(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_simple_counter, this)
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        val vertical = 4.dpToPx(resources)
        val horizontal = 2.dpToPx(resources)
        setBackgroundResource(context.readAttr(R.attr.selectableItemBackgroundBorderless))
        updatePadding(
            left = horizontal,
            right = horizontal,
            top = vertical,
            bottom = vertical,
        )
    }
}

fun SimpleCounter.bind(value: Button) {
    val binding = ViewSimpleCounterBinding.bind(this)
    val color = value.color?.toColorInt(context) ?: context.readColorAttr(R.attr.colorControlNormal)

    setOnClick(value.clickAction)
    binding.txtCount.setTextColor(color)
    binding.txtCount.text = value.label
    binding.imgIcon.setImage(value.icon)
    binding.imgIcon.imageTintList = color
}
