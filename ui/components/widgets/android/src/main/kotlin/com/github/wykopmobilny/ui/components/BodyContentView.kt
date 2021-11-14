package com.github.wykopmobilny.ui.components

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.method.Touch
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.getSpans
import androidx.core.view.isVisible
import io.github.wykopmobilny.ui.components.widgets.MessageBodyUi
import java.util.regex.Pattern


class BodyContentView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {

    private val ellipsis = SpannableString("\u0020[rozwiń]")
    private val defaultEndPunctuation = Pattern.compile("[.!?,;:…]*$", Pattern.DOTALL)
    var rawText: CharSequence? = null

    var onViewMoreClicked: () -> Unit = {}

    init {
        movementMethod = SelectableLinkMovement
        ellipsize = TextUtils.TruncateAt.END
        ellipsis.setSpan(
            ViewMoreSpan(),
            1,
            ellipsis.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (!hasOnClickListeners() && !linksClickable) {
            // Avoid all touches
            return false
        }

        // Handle avoiding touch events, if not on a clickable span
        if (!hasOnClickListeners()) {
            if (layout != null) {
                val xTarget = event.x - totalPaddingLeft + scrollX
                val yTarget = event.y - totalPaddingTop + scrollY
                val line = layout.getLineForVertical(yTarget.toInt())
                val off = layout.getOffsetForHorizontal(line, xTarget)

                val spansAtPoint = (text as? Spanned)?.getSpans<ClickableSpan>(start = off, end = off)
                if (spansAtPoint.isNullOrEmpty()) {
                    // No clickable span at event -- abort touch
                    return false
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        if (text !is Spanned || text.getSpans<ViewMoreSpan>().isEmpty()) {
            rawText = text
        }
    }

    private val ellipsizeStrategy = EllipsizeEndStrategy()
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val processed = ellipsizeStrategy.processText(rawText)
        if (processed.toString() != text.toString()) {
            text = processed
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private inner class EllipsizeEndStrategy {

        fun processText(text: CharSequence?) =
            if (text == null || fitsInLayout(text)) text else createEllipsizedText(text)

        private fun fitsInLayout(text: CharSequence) =
            createWorkingLayout(text).lineCount <= maxLines

        private fun createWorkingLayout(workingText: CharSequence) =
            StaticLayout.Builder.obtain(
                workingText,
                0,
                workingText.length,
                paint,
                measuredWidth - compoundPaddingLeft - compoundPaddingRight,
            )
                .setMaxLines(maxLines)
                .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
                .build()

        private fun stripEndPunctuation(workingText: CharSequence) =
            defaultEndPunctuation.matcher(workingText).replaceFirst("")

        private fun createEllipsizedText(fullText: CharSequence): CharSequence {
            val layout = createWorkingLayout(fullText)
            val cutOffIndex = layout.getLineEnd((maxLines - 1).coerceAtMost(layout.lineCount))
            val textLength = fullText.length
            val cutOffLength = (textLength - cutOffIndex).coerceAtLeast(ellipsis.length)
            var workingText: CharSequence = TextUtils.substring(fullText, 0, textLength - cutOffLength).trim()
            while (!fitsInLayout(TextUtils.concat(stripEndPunctuation(workingText), ellipsis))) {
                val lastSpace = TextUtils.lastIndexOf(workingText, ' ')
                if (lastSpace == -1) {
                    break
                }
                workingText = TextUtils.substring(workingText, 0, lastSpace).trim()
            }
            workingText = TextUtils.concat(stripEndPunctuation(workingText), ellipsis)
            val dest = SpannableStringBuilder(workingText)
            if (fullText is Spanned) {
                TextUtils.copySpansFrom(fullText, 0, workingText.length - ellipsis.length, null, dest, 0)
            }
            return dest
        }
    }

    private inner class ViewMoreSpan : ClickableSpan() {

        override fun onClick(widget: View) = onViewMoreClicked()
    }
}

private object SelectableLinkMovement : LinkMovementMethod() {

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_UP) {
            val xTarget = event.x - widget.totalPaddingLeft + widget.scrollX
            val yTarget = event.y - widget.totalPaddingTop + widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(yTarget.toInt())
            val off = layout.getOffsetForHorizontal(line, xTarget)
            val link = buffer.getSpans<ClickableSpan>(start = off, end = off).firstOrNull()
            if (link != null) {
                link.onClick(widget)
                return true
            }
        }
        return Touch.onTouchEvent(widget, buffer, event)
    }
}


fun BodyContentView.bind(body: MessageBodyUi) {
    isVisible = body.content != null
    onViewMoreClicked = body.viewMoreAction
    maxLines = body.maxLines
    text = body.content
}
