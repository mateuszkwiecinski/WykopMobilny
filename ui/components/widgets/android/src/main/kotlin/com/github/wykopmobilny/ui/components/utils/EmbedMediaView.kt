package com.github.wykopmobilny.ui.components.utils

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import io.github.wykopmobilny.ui.components.widgets.EmbedMediaUi
import io.github.wykopmobilny.ui.components.widgets.NSFW_PLACEHOLDER
import io.github.wykopmobilny.ui.components.widgets.android.R
import io.github.wykopmobilny.ui.components.widgets.android.databinding.ViewEmbedMediaBinding
import io.github.wykopmobilny.utils.bindings.setOnClick

class EmbedMediaView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_embed_media, this)
        foreground = ContextCompat.getDrawable(context, context.readAttr(R.attr.selectableItemBackground))
    }
}

fun EmbedMediaView.bind(model: EmbedMediaUi?) {
    val binding = ViewEmbedMediaBinding.bind(this)
    binding.root.setOnClick(model?.clickAction)
    binding.root.isVisible = model != null


    binding.fullOverlay.isVisible = model?.hasNsfwOverlay == true
    binding.imgPreview.isVisible = model?.hasNsfwOverlay != true
    if (model?.hasNsfwOverlay == true) {
        Glide.with(this).load(NSFW_PLACEHOLDER)
            .into(binding.fullOverlay)
    }

    val url = when (val content = model?.content) {
        is EmbedMediaUi.Content.StaticImage -> content.url
        is EmbedMediaUi.Content.PlayableMedia -> content.previewImage
        null -> null
    }
    Glide.with(this).load(url)
        .transition(withCrossFade())
        .into(binding.imgPreview)

    val prompt = when (val content = model?.content) {
        is EmbedMediaUi.Content.StaticImage -> null
        is EmbedMediaUi.Content.PlayableMedia -> content.domain
        null -> null
    }
    binding.imgPromptBackground.isVisible = prompt != null
    binding.txtPrompt.isVisible = prompt != null
    binding.txtPrompt.text = prompt
    binding.txtSize.isVisible = model?.size != null
    binding.txtSize.text =  model?.size
}
