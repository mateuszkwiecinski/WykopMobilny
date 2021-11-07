package com.github.wykopmobilny.ui.components.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import io.github.wykopmobilny.ui.components.widgets.EmbedMediaUi
import io.github.wykopmobilny.ui.components.widgets.android.R
import io.github.wykopmobilny.ui.components.widgets.android.databinding.ViewEmbedMediaBinding
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class EmbedMediaView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_embed_media, this)
        isClickable = true
        isFocusable = true
    }
}

fun EmbedMediaView.bind(model: EmbedMediaUi?) {
    val binding = ViewEmbedMediaBinding.bind(this)
    binding.root.isVisible = model != null
    model ?: return

    val threshold = model.thresholdPercentage
    binding.imgPreview.maxHeight = if (model.forceExpanded || threshold == null) {
        Int.MAX_VALUE
    } else {
        // (context.resources.displayMetrics.heightPixels / 100f * threshold).toInt()
        100.dpToPx(resources)
    }
    Glide.with(this).load(model.previewUrl)
        .transform(WykopSpecificTransformation())
        .transition(withCrossFade())
        .centerCrop()
        .into(binding.imgPreview)
}

private class WykopSpecificTransformation : BitmapTransformation() {

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        if (toTransform.width == outWidth && toTransform.height == outHeight) {
            return toTransform
        }
        val scale: Float
        val dx: Float
        val dy: Float
        val matrix = Matrix()
        if (toTransform.width * outHeight > outWidth * toTransform.height) {
            scale = outHeight.toFloat() / toTransform.height
            dx = (outWidth - toTransform.width * scale) * 0.5f
            dy = 0f
        } else {
            scale = outWidth.toFloat() / toTransform.width
            dx = 0f
            dy = (outHeight - toTransform.height * scale) * 0.5f
        }

        matrix.setScale(scale, scale)
        matrix.postTranslate((dx + 0.5f).toInt().toFloat(), (dy + 0.5f).toInt().toFloat())

        val result = pool[outWidth, outHeight, toTransform.config ?: Bitmap.Config.ARGB_8888]
        TransformationUtils.setAlpha(toTransform, result)


        applyMatrix(toTransform, result, matrix)

        return result
    }

    private fun applyMatrix(inBitmap: Bitmap, targetBitmap: Bitmap, matrix: Matrix) {
        BITMAP_DRAWABLE_LOCK.lock()
        try {
            val canvas = Canvas(targetBitmap)
            canvas.drawBitmap(inBitmap, matrix, DEFAULT_PAINT)
            canvas.setBitmap(null)
        } finally {
            BITMAP_DRAWABLE_LOCK.unlock()
        }
    }

    override fun equals(other: Any?) = other is WykopSpecificTransformation

    override fun hashCode() = ID.hashCode()

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    companion object {
        private const val ID = "wykop.specific.transform"
        private val ID_BYTES = ID.toByteArray(CHARSET)

        private val MODELS_REQUIRING_BITMAP_LOCK = setOf(
            // Moto X gen 2
            "XT1085",
            "XT1092",
            "XT1093",
            "XT1094",
            "XT1095",
            "XT1096",
            "XT1097",
            "XT1098",
            // Moto G gen 1
            "XT1031",
            "XT1028",
            "XT937C",
            "XT1032",
            "XT1008",
            "XT1033",
            "XT1035",
            "XT1034",
            "XT939G",
            "XT1039",
            "XT1040",
            "XT1042",
            "XT1045",
            // Moto G gen 2
            "XT1063",
            "XT1064",
            "XT1068",
            "XT1069",
            "XT1072",
            "XT1077",
            "XT1078",
            "XT1079",
        )
        private val DEFAULT_PAINT = Paint(Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val BITMAP_DRAWABLE_LOCK = if (MODELS_REQUIRING_BITMAP_LOCK.contains(android.os.Build.MODEL)) {
            ReentrantLock()
        } else {
            NoLock()
        }
    }
}

private class NoLock : Lock {

    override fun lock() = Unit

    override fun lockInterruptibly() = Unit

    override fun tryLock() = true

    override fun tryLock(time: Long, unit: TimeUnit?) = true

    override fun unlock() = Unit

    override fun newCondition(): Condition = error("Shouldn't be called")

}
