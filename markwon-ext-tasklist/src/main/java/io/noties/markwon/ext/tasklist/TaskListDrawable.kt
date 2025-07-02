package io.noties.markwon.ext.tasklist

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import kotlin.math.min

/**
 * @since 1.0.1
 */
class TaskListDrawable(
    @param:ColorInt private val checkedFillColor: Int,
    @param:ColorInt private val normalOutlineColor: Int,
    @ColorInt checkMarkColor: Int
) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    private val checkMarkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val checkMarkPath = Path()

    private var isChecked = false

    // unfortunately we cannot rely on TextView to be LAYER_TYPE_SOFTWARE
    // if we could we would draw our checkMarkPath with PorterDuff.CLEAR
    init {
        checkMarkPaint.setColor(checkMarkColor)
        checkMarkPaint.style = Paint.Style.STROKE
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        // we should exclude stroke with from final bounds (half of the strokeWidth from all sides)

        // we should have square shape
        val min = min(bounds.width(), bounds.height()).toFloat()
        val stroke = min / 8

        val side = min - stroke
        rectF.set(0f, 0f, side, side)

        paint.strokeWidth = stroke
        checkMarkPaint.strokeWidth = stroke

        checkMarkPath.reset()

        POINT_0.moveTo(checkMarkPath, side)
        POINT_1.lineTo(checkMarkPath, side)
        POINT_2.lineTo(checkMarkPath, side)
    }

    override fun draw(canvas: Canvas) {
        val style: Paint.Style
        val color: Int

        if (isChecked) {
            style = Paint.Style.FILL_AND_STROKE
            color = checkedFillColor
        } else {
            style = Paint.Style.STROKE
            color = normalOutlineColor
        }
        paint.style = style
        paint.setColor(color)

        val bounds = getBounds()

        val left = (bounds.width() - rectF.width()) / 2
        val top = (bounds.height() - rectF.height()) / 2

        val radius = rectF.width() / 8

        val save = canvas.save()
        try {
            canvas.translate(left, top)

            canvas.drawRoundRect(rectF, radius, radius, paint)

            if (isChecked) {
                canvas.drawPath(checkMarkPath, checkMarkPaint)
            }
        } finally {
            canvas.restoreToCount(save)
        }
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {
        paint.setAlpha(alpha)
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.setColorFilter(colorFilter)
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun isStateful(): Boolean {
        return true
    }

    override fun onStateChange(state: IntArray): Boolean {
        val checked: Boolean

        val length = state.size

        if (length > 0) {
            var inner = false

            for (i in 0..<length) {
                if (android.R.attr.state_checked == state[i]) {
                    inner = true
                    break
                }
            }
            checked = inner
        } else {
            checked = false
        }

        val result = checked != isChecked
        if (result) {
            invalidateSelf()
            isChecked = checked
        }

        return result
    }

    private class Point(val x: Float, val y: Float) {
        fun moveTo(path: Path, side: Float) {
            path.moveTo(side * x, side * y)
        }

        fun lineTo(path: Path, side: Float) {
            path.lineTo(side * x, side * y)
        }
    }

    companion object {
        // represent ratios (not exact coordinates)
        private val POINT_0 = Point(2.75f / 18, 8.25f / 18)
        private val POINT_1 = Point(7f / 18, 12.5f / 18)
        private val POINT_2 = Point(15.25f / 18, 4.75f / 18)
    }
}
