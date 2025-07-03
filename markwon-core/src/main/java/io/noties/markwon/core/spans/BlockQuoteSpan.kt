package io.noties.markwon.core.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.style.LeadingMarginSpan
import io.noties.markwon.core.MarkwonTheme
import kotlin.math.max
import kotlin.math.min

data class BlockQuoteSpan(
    private val theme: MarkwonTheme
) : LeadingMarginSpan {
    private val rect: Rect = ObjectsPool.rect()
    private val paint: Paint = ObjectsPool.paint()

    override fun getLeadingMargin(first: Boolean): Int {
        return theme.blockMargin
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
        val width = theme.getBlockQuoteWidth()

        paint.set(p)

        theme.applyBlockQuoteStyle(paint)

        val left: Int
        val right: Int
        run {
            val l = x + (dir * width)
            val r = l + (dir * width)
            left = min(l, r)
            right = max(l, r)
        }

        rect.set(left, top, right, bottom)

        c.drawRect(rect, paint)
    }
}
