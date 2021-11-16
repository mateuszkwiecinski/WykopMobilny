package com.github.wykopmobilny.ui.components

import android.content.Context
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import com.github.wykopmobilny.ui.components.utils.readColorAttr
import io.github.wykopmobilny.ui.components.widgets.TwoActionsCounterUi
import io.github.wykopmobilny.ui.components.widgets.android.R
import io.github.wykopmobilny.ui.components.widgets.android.databinding.ViewLinkVoteButtonBinding
import io.github.wykopmobilny.utils.bindings.setOnClick
import io.github.wykopmobilny.utils.bindings.toColorInt

class LinkVoteButton(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_link_vote_button, this)
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundResource(R.drawable.ripple_outline)
    }
}

fun LinkVoteButton.bind(value: TwoActionsCounterUi) {
    val binding = ViewLinkVoteButtonBinding.bind(this)
    val stroke = value.color?.toColorInt(context) ?: context.readColorAttr(R.attr.colorOutline)
    val color = value.color?.toColorInt(context) ?: context.readColorAttr(R.attr.colorControlNormal)

    (background.mutate() as RippleDrawable).getDrawable(1).mutate().setTintList(stroke)
    binding.txtCount.text = value.count.toString()
    binding.txtCount.setTextColor(color)
    binding.imgVoteUp.imageTintList = color
    binding.imgVoteUp.isEnabled = value.upvoteAction != null
    binding.imgVoteUp.setOnClick(value.upvoteAction)
    binding.imgVoteDown.imageTintList = color
    binding.imgVoteDown.isEnabled = value.downvoteAction != null
    binding.imgVoteDown.setOnClick(value.downvoteAction)
}
