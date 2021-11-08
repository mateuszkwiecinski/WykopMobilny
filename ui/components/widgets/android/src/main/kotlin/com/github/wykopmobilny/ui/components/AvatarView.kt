package com.github.wykopmobilny.ui.components

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import io.github.wykopmobilny.ui.components.widgets.AvatarUi
import io.github.wykopmobilny.ui.components.widgets.android.R
import io.github.wykopmobilny.ui.components.widgets.android.databinding.ViewAvatarSimpleBinding
import io.github.wykopmobilny.utils.bindings.setOnClick
import io.github.wykopmobilny.utils.bindings.toColorInt

class AvatarView(
    context: Context,
    attributeSet: AttributeSet?,
) : ConstraintLayout(context, attributeSet) {

    init {
        inflate(context, R.layout.view_avatar_simple, this)
    }
}

fun AvatarView.bind(model: AvatarUi?) {
    val binding = ViewAvatarSimpleBinding.bind(this)
    Glide.with(binding.imgAvatar)
        .load(model?.avatarUrl)
        .circleCrop()
        .into(binding.imgAvatar)
    binding.imgAvatar.setOnClick(model?.onClicked)
    Glide.with(binding.imgGenderStrip)
        .load(model?.genderStrip.toColorInt(context).defaultColor.let(::ColorDrawable))
        .circleCrop()
        .into(binding.imgGenderStrip)
}
