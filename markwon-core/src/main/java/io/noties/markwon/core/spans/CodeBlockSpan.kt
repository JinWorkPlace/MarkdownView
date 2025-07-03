package io.noties.markwon.core.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.TextPaint
import android.text.style.LeadingMarginSpan
import android.text.style.MetricAffectingSpan
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.ObjectsPool.paint
import io.noties.markwon.core.spans.ObjectsPool.rect

/**
 * @since 3.0.0 split inline and block spans
 */
class CodeBlockSpan(
    private val theme: MarkwonTheme
) : MetricAffectingSpan(), LeadingMarginSpan {
    private val rect = rect()
    private val paint = paint()

    override fun updateMeasureState(p: TextPaint) {
        apply(p)
    }

    override fun updateDrawState(ds: TextPaint) {
        apply(ds)
    }

    private fun apply(p: TextPaint) {
        theme.applyCodeBlockTextStyle(p)
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return theme.codeBlockMargin
    }

    override fun drawLeadingMargin(
        c: Canvas,
        p: Paint,
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
        paint.style = Paint.Style.FILL
        paint.color = theme.getCodeBlockBackgroundColor(p)

        val left: Int
        val right: Int
        if (dir > 0) {
            left = x
            right = c.width
        } else {
            left = x - c.width
            right = x
        }

        rect.set(left, top, right, bottom)

        c.drawRect(rect, paint)
    }
}
