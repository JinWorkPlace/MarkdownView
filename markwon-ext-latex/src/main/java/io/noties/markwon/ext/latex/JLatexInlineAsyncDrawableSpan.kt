package io.noties.markwon.ext.latex

import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import io.noties.markwon.core.MarkwonTheme

/**
 * @since 4.3.0
 */
internal class JLatexInlineAsyncDrawableSpan(
    theme: MarkwonTheme,
    override val drawable: JLatextAsyncDrawable,
    @ColorInt color: Int
) : JLatexAsyncDrawableSpan(theme, drawable, color) {

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        @IntRange(from = 0) start: Int,
        @IntRange(from = 0) end: Int,
        fm: FontMetricsInt?
    ): Int {
        // if we have no async drawable result - we will just render text

        val size: Int

        if (drawable.hasResult()) {
            val rect = drawable.bounds

            if (fm != null) {
                val half = rect.bottom / 2
                fm.ascent = -half
                fm.descent = half

                fm.top = fm.ascent
                fm.bottom = 0
            }

            size = rect.right
        } else {
            // NB, no specific text handling (no new lines, etc)

            size = (paint.measureText(text, start, end) + .5f).toInt()
        }

        return size
    }
}
