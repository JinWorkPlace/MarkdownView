package io.noties.markwon.utils

import android.content.Context

data class Dip(
    private val density: Float
) {
    fun toPx(dp: Int): Int {
        return (dp * density + .5f).toInt()
    }

    companion object {
        @JvmStatic
        fun create(context: Context): Dip {
            return Dip(context.resources.displayMetrics.density)
        }

        fun create(density: Float): Dip {
            return Dip(density)
        }
    }
}
