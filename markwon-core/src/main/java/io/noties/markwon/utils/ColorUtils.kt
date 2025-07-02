package io.noties.markwon.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange

object ColorUtils {
    @JvmStatic
    @ColorInt
    fun applyAlpha(
        @ColorInt color: Int, @IntRange(from = 0, to = 255) alpha: Int
    ): Int {
        return (color and 0x00FFFFFF) or (alpha shl 24)
    }

    // blend two colors w/ specified ratio, resulting color won't have alpha channel
    @ColorInt
    fun blend(
        @ColorInt foreground: Int,
        @ColorInt background: Int,
        @FloatRange(from = 0.0, to = 1.0) ratio: Float
    ): Int {
        return Color.rgb(
            (((1f - ratio) * Color.red(foreground)) + (ratio * Color.red(background))).toInt(),
            (((1f - ratio) * Color.green(foreground)) + (ratio * Color.green(background))).toInt(),
            (((1f - ratio) * Color.blue(foreground)) + (ratio * Color.blue(background))).toInt()
        )
    }
}
