package com.github.wykopmobilny.ui.components.utils

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import io.github.wykopmobilny.ui.components.widgets.EmbedMediaUi
import io.github.wykopmobilny.ui.components.widgets.android.R
import io.github.wykopmobilny.ui.components.widgets.android.databinding.ViewEmbedMediaBinding

class EmbedMediaView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_embed_media, this)
    }
}

fun EmbedMediaView.bind(model: EmbedMediaUi?) {
    val binding = ViewEmbedMediaBinding.bind(this)
    binding.root.isVisible = model != null
    model ?: return

    Glide.with(this).load(model.previewUrl)
        .transition(withCrossFade())
        .into(binding.imgPreview)
}
