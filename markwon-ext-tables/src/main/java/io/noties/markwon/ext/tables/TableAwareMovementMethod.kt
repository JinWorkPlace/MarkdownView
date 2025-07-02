package io.noties.markwon.ext.tables

import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.text.style.ClickableSpan
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.TextView

/**
 * @since 4.6.0
 */
class TableAwareMovementMethod(private val wrapped: MovementMethod) : MovementMethod {
    override fun initialize(widget: TextView?, text: Spannable?) {
        wrapped.initialize(widget, text)
    }

    override fun onKeyDown(
        widget: TextView?,
        text: Spannable?,
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {
        return wrapped.onKeyDown(widget, text, keyCode, event)
    }

    override fun onKeyUp(
        widget: TextView?,
        text: Spannable?,
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {
        return wrapped.onKeyUp(widget, text, keyCode, event)
    }

    override fun onKeyOther(view: TextView?, text: Spannable?, event: KeyEvent?): Boolean {
        return wrapped.onKeyOther(view, text, event)
    }

    override fun onTakeFocus(widget: TextView?, text: Spannable?, direction: Int) {
        wrapped.onTakeFocus(widget, text, direction)
    }

    override fun onTrackballEvent(
        widget: TextView?,
        text: Spannable?,
        event: MotionEvent?
    ): Boolean {
        return wrapped.onTrackballEvent(widget, text, event)
    }

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        // let wrapped handle first, then if super handles nothing, search for table row spans
        return wrapped.onTouchEvent(widget, buffer, event)
                || handleTableRowTouchEvent(widget, buffer, event)
    }

    override fun onGenericMotionEvent(
        widget: TextView?,
        text: Spannable?,
        event: MotionEvent?
    ): Boolean {
        return wrapped.onGenericMotionEvent(widget, text, event)
    }

    override fun canSelectArbitrarily(): Boolean {
        return wrapped.canSelectArbitrarily()
    }

    companion object {
        fun wrap(movementMethod: MovementMethod): TableAwareMovementMethod {
            return TableAwareMovementMethod(movementMethod)
        }

        /**
         * Wraps LinkMovementMethod
         */
        fun create(): TableAwareMovementMethod {
            return TableAwareMovementMethod(LinkMovementMethod.getInstance())
        }

        fun handleTableRowTouchEvent(
            widget: TextView,
            buffer: Spannable,
            event: MotionEvent
        ): Boolean {
            // handle only action up (originally action down is used in order to handle selection,
            //  which tables do no have)
            if (event.action != MotionEvent.ACTION_UP) {
                return false
            }

            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            val spans = buffer.getSpans(off, off, TableRowSpan::class.java)
            if (spans.size == 0) {
                return false
            }

            val span = spans[0]

            // okay, we can calculate the x to obtain span, but what about y?
            val rowLayout = span.findLayoutForHorizontalOffset(x)
            if (rowLayout != null) {
                // line top as basis
                val rowY = layout.getLineTop(line)
                val rowLine = rowLayout.getLineForVertical(y - rowY)
                val rowOffset =
                    rowLayout.getOffsetForHorizontal(rowLine, (x % span.cellWidth()).toFloat())
                val rowClickableSpans = (rowLayout.text as Spanned)
                    .getSpans(rowOffset, rowOffset, ClickableSpan::class.java)
                if (rowClickableSpans.size > 0) {
                    rowClickableSpans[0]!!.onClick(widget)
                    return true
                }
            }

            return false
        }
    }
}
