package io.noties.markwon.core.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.ObjectsPool.paint
import io.noties.markwon.core.spans.ObjectsPool.rect

class ThematicBreakSpan(private val theme: MarkwonTheme) : LeadingMarginSpan {
    private val rect = rect()
    private val paint = paint()

    override fun getLeadingMargin(first: Boolean): Int {
        return 0
    }

    override fun drawLeadingMargin(
        c: Canvas,
        p: Paint?,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence?,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout?
    ) {
        val middle = top + ((bottom - top) / 2)

        paint.set(p)
        theme.applyThematicBreakStyle(paint)

        val height = (paint.strokeWidth + .5f).toInt()
        val halfHeight = (height / 2f + .5f).toInt()

        val left: Int
        val right: Int
        if (dir > 0) {
            left = x
            right = c.width
        } else {
            left = x - c.width
            right = x
        }

        rect.set(left, middle - halfHeight, right, middle + halfHeight)
        c.drawRect(rect, paint)
    }
}
