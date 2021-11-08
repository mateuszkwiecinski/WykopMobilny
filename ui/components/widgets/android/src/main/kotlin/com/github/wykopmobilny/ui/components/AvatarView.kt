package com.github.wykopmobilny.ui.components

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
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
    val transformation = CircleCrop()

    val requestOptions = RequestOptions()
        .centerCrop()
        .transform(transformation)

    val placeholder = Glide.with(context)
        .load(R.drawable.avatar)
        .apply(requestOptions)
    Glide.with(binding.imgAvatar)
        .load(model?.avatarUrl)
        .apply(requestOptions)
        .thumbnail(placeholder)
        .circleCrop()
        .into(binding.imgAvatar)
    binding.imgAvatar.setOnClick(model?.onClicked)
    Glide.with(binding.imgGenderStrip)
        .load(model?.genderStrip.toColorInt(context).defaultColor.let(::ColorDrawable))
        .dontAnimate()
        .circleCrop()
        .into(binding.imgGenderStrip)
}
