package io.noties.markwon.core.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.TextPaint
import android.text.style.LeadingMarginSpan
import android.text.style.MetricAffectingSpan
import androidx.annotation.IntRange
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.utils.LeadingMarginUtils

data class HeadingSpan(
    private val theme: MarkwonTheme,
    /**
     * @since 4.2.0
     */
    @param:IntRange(from = 1, to = 6) val level: Int
) : MetricAffectingSpan(), LeadingMarginSpan {
    private val rect: Rect = ObjectsPool.rect()
    private val paint: Paint = ObjectsPool.paint()

    override fun updateMeasureState(p: TextPaint) {
        apply(p)
    }

    override fun updateDrawState(tp: TextPaint) {
        apply(tp)
    }

    private fun apply(paint: TextPaint) {
        theme.applyHeadingTextStyle(paint, level)
    }

    override fun getLeadingMargin(first: Boolean): Int {
        // no margin actually, but we need to access Canvas to draw break
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
        if ((level == 1 || level == 2) && LeadingMarginUtils.selfEnd(end, text, this)) {
            paint.set(p)

            theme.applyHeadingBreakStyle(paint)

            val height = paint.strokeWidth

            if (height > .0f) {
                val b = (bottom - height + .5f).toInt()

                val left: Int
                val right: Int
                if (dir > 0) {
                    left = x
                    right = c.width
                } else {
                    left = x - c.width
                    right = x
                }

                rect.set(left, b, right, bottom)
                c.drawRect(rect, paint)
            }
        }
    }
}
