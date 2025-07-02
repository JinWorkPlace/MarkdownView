package io.noties.markwon.utils

import android.graphics.drawable.Drawable

@Deprecated("Please use {@link io.noties.markwon.image.DrawableUtils}")
object DrawableUtils {
    fun intrinsicBounds(drawable: Drawable) {
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    }
}
