package io.noties.markwon.image

import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.CheckResult

/**
 * @since 3.0.1
 */
object DrawableUtils {
    @JvmStatic
    @CheckResult
    fun intrinsicBounds(drawable: Drawable): Rect {
        return Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    }

    @JvmStatic
    fun applyIntrinsicBounds(drawable: Drawable) {
        drawable.bounds = intrinsicBounds(drawable)
    }

    @JvmStatic
    fun applyIntrinsicBoundsIfEmpty(drawable: Drawable) {
        if (drawable.bounds.isEmpty) {
            drawable.bounds = intrinsicBounds(drawable)
        }
    }
}
