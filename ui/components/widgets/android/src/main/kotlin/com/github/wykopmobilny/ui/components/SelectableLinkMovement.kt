package com.github.wykopmobilny.ui.components

import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.method.Touch
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.TextView


object SelectableLinkMovement : LinkMovementMethod() {

    override fun canSelectArbitrarily() = true

    override fun initialize(widget: TextView?, text: Spannable) {
        Selection.setSelection(text, text.length)
    }

    override fun onTakeFocus(view: TextView, text: Spannable, dir: Int) {
        if (dir and (View.FOCUS_FORWARD or View.FOCUS_DOWN) != 0) {
            if (view.layout == null) {
                // This shouldn't be null, but do something sensible if it is.
                Selection.setSelection(text, text.length)
            }
        } else {
            Selection.setSelection(text, text.length)
        }
    }

    override fun onTouchEvent(
        widget: TextView, buffer: Spannable,
        event: MotionEvent,
    ): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_DOWN
        ) {
            val xTarget = event.x - widget.totalPaddingLeft + widget.scrollX
            val yTarget = event.y - widget.totalPaddingTop + widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(yTarget.toInt())
            val off = layout.getOffsetForHorizontal(line, xTarget)
            val link = buffer.getSpans(off, off, ClickableSpan::class.java).firstOrNull()
            if (link != null) {
                when (action) {
                    MotionEvent.ACTION_UP -> link.onClick(widget)
                    MotionEvent.ACTION_DOWN -> Selection.setSelection(buffer, buffer.getSpanStart(link), buffer.getSpanEnd(link))
                }
                return true
            }
        }
        return Touch.onTouchEvent(widget, buffer, event)
    }
}
