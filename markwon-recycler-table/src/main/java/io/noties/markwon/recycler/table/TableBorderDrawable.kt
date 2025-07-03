package io.noties.markwon.recycler.table

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px

internal class TableBorderDrawable : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        if (paint.strokeWidth > 0) {
            canvas.drawRect(bounds, paint)
        }
    }

    override fun setAlpha(alpha: Int) {
        // no op
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // no op
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    fun update(@Px borderWidth: Int, @ColorInt color: Int) {
        paint.strokeWidth = borderWidth.toFloat()
        paint.color = color
        invalidateSelf()
    }
}
