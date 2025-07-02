package io.noties.markwon.ext.latex

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.image.AsyncDrawableSpan
import ru.noties.jlatexmath.JLatexMathDrawable
import ru.noties.jlatexmath.awt.Color

/**
 * @since 4.3.0
 */
open class JLatexAsyncDrawableSpan(
    theme: MarkwonTheme,
    private val drawable: JLatextAsyncDrawable,
    @param:ColorInt private val color: Int
) : AsyncDrawableSpan(
    theme, drawable, ALIGN_CENTER, false
) {
    private var appliedTextColor: Boolean

    init {
        // if color is not 0 -> then no need to apply text color
        this.appliedTextColor = color != 0
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        if (!appliedTextColor && drawable.hasResult()) {
            // it is important to check for type (in case of an error, or custom placeholder or whatever
            //  this result can be of other type)
            val drawableResult = drawable.result
            if (drawableResult is JLatexMathDrawable) {
                val result = drawableResult
                val icon = result.icon()
                icon.setForeground(Color(paint.color))
                appliedTextColor = true
            }
        }
        super.draw(canvas, text, start, end, x, top, y, bottom, paint)
    }

    private fun drawable(): JLatextAsyncDrawable {
        return drawable
    }

    @ColorInt
    fun color(): Int {
        return color
    }
}
