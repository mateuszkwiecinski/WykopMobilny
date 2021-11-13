package com.github.wykopmobilny.ui.components

import android.content.Context
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.view.updatePadding
import com.github.wykopmobilny.ui.components.utils.dpToPx
import com.github.wykopmobilny.ui.components.utils.readColorAttr
import io.github.wykopmobilny.ui.components.widgets.Button
import io.github.wykopmobilny.ui.components.widgets.android.R
import io.github.wykopmobilny.ui.components.widgets.android.databinding.ViewFlatButtonBinding
import io.github.wykopmobilny.utils.bindings.setImage
import io.github.wykopmobilny.utils.bindings.setOnClick
import io.github.wykopmobilny.utils.bindings.toColorInt

class FlatButton(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_flat_button, this)
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundResource(R.drawable.ripple_outline)
        val vertical = 4.dpToPx(resources)
        val horizontal = 6.dpToPx(resources)
        updatePadding(
            left = horizontal,
            right = horizontal,
            top = vertical,
            bottom = vertical,
        )
    }
}

fun FlatButton.bind(value: Button) {
    val binding = ViewFlatButtonBinding.bind(this)
    val stroke = value.color?.toColorInt(context) ?: context.readColorAttr(R.attr.colorOutline)
    val color = value.color?.toColorInt(context) ?: context.readColorAttr(R.attr.colorControlNormal)

    (background.mutate() as RippleDrawable).getDrawable(1).mutate().setTintList(stroke)
    setOnClick(value.clickAction)
    binding.txtCount.setTextColor(color)
    binding.txtCount.text = value.label
    binding.imgIcon.imageTintList = color
    binding.imgIcon.setImage(value.icon)
}
