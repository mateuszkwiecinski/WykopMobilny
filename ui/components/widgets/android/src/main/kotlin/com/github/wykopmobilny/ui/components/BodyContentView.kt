package com.github.wykopmobilny.ui.components

import android.content.Context
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.Touch
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.getSpans


class BodyContentView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {

    init {
        movementMethod = SelectableLinkMovement
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
