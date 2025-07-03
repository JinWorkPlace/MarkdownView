package io.noties.markwon.core.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.text.Layout
import android.text.style.LeadingMarginSpan
import androidx.annotation.IntRange
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.utils.LeadingMarginUtils
import kotlin.math.max
import kotlin.math.min

class BulletListItemSpan(
    private val theme: MarkwonTheme, @param:IntRange(from = 0) private val level: Int
) : LeadingMarginSpan {
    private val paint: Paint = ObjectsPool.paint()
    private val circle: RectF = ObjectsPool.rectF()
    private val rectangle: Rect = ObjectsPool.rect()

    override fun getLeadingMargin(first: Boolean): Int {
        return theme.getBlockMargin()
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
        layout: Layout
    ) {
        // if there was a line break, we don't need to draw anything

        if (!first || !LeadingMarginUtils.selfStart(start, text, this)) {
            return
        }

        paint.set(p)

        theme.applyListItemStyle(paint)

        val save = c.save()
        try {
            val width = theme.getBlockMargin()

            // @since 1.0.6 we no longer rely on (bottom-top) calculation in order to detect line height
            // it lead to bad rendering as first & last lines received different results even
            // if text size is the same (first line received greater amount and bottom line -> less)
            val textLineHeight = (paint.descent() - paint.ascent() + .5f).toInt()

            val side = theme.getBulletWidth(textLineHeight)

            val marginLeft = (width - side) / 2

            // in order to support RTL
            val l: Int
            val r: Int
            run {
                // @since 4.2.1 to correctly position bullet
                // when nested inside other LeadingMarginSpans (sorry, Nougat)
                if (IS_NOUGAT) {
                    // @since 2.0.2
                    // There is a bug in Android Nougat, when this span receives an `x` that
                    // doesn't correspond to what it should be (text is placed correctly though).
                    // Let's make this a general rule -> manually calculate difference between expected/actual
                    // and add this difference to resulting left/right values. If everything goes well
                    // we do not encounter a bug -> this `diff` value will be 0
                    val diff: Int = if (dir < 0) {
                        x - (layout.width - (width * level))
                    } else {
                        (width * level) - x
                    }

                    val left = x + (dir * marginLeft)
                    val right = left + (dir * side)
                    l = min(left, right) + (dir * diff)
                    r = max(left, right) + (dir * diff)
                } else {
                    l = if (dir > 0) {
                        x + marginLeft
                    } else {
                        x - width + marginLeft
                    }
                    r = l + side
                }
            }

            val t = baseline + ((paint.descent() + paint.ascent()) / 2f + .5f).toInt() - (side / 2)
            val b = t + side

            if (level == 0 || level == 1) {
                circle.set(l.toFloat(), t.toFloat(), r.toFloat(), b.toFloat())

                val style = if (level == 0) Paint.Style.FILL else Paint.Style.STROKE
                paint.style = style

                c.drawOval(circle, paint)
            } else {
                rectangle.set(l, t, r, b)

                paint.style = Paint.Style.FILL

                c.drawRect(rectangle, paint)
            }
        } finally {
            c.restoreToCount(save)
        }
    }

    companion object {
        private val IS_NOUGAT: Boolean

        init {
            val sdk = Build.VERSION.SDK_INT
            IS_NOUGAT = Build.VERSION_CODES.N == sdk || Build.VERSION_CODES.N_MR1 == sdk
        }
    }
}
