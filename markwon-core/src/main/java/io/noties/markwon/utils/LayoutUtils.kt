package io.noties.markwon.utils

import android.text.Layout

/**
 * @since 4.4.0
 */
object LayoutUtils {
    private const val DEFAULT_EXTRA = 0F
    private const val DEFAULT_MULTIPLIER = 1F

    fun getLineBottomWithoutPaddingAndSpacing(layout: Layout, line: Int): Int {
        val bottom = layout.getLineBottom(line)
        val isSpanLastLine = line == layout.lineCount - 1
        val lineSpacingExtra = layout.spacingAdd
        val lineSpacingMultiplier = layout.spacingMultiplier

        // Simplified check
        val hasLineSpacing =
            lineSpacingExtra != DEFAULT_EXTRA || lineSpacingMultiplier != DEFAULT_MULTIPLIER

        val lineBottom = if (!hasLineSpacing || isSpanLastLine) {
            bottom
        } else {
            val extra = if (DEFAULT_MULTIPLIER.compareTo(lineSpacingMultiplier) != 0) {
                val lineHeight = getLineHeight(layout, line)
                lineHeight - ((lineHeight - lineSpacingExtra) / lineSpacingMultiplier)
            } else {
                lineSpacingExtra
            }
            (bottom - extra + 0.5F).toInt()
        }

        // Check if it is the last line that span is occupying and that this line is the last one in TextView
        return if (isSpanLastLine && line == layout.lineCount - 1) {
            lineBottom - layout.bottomPadding
        } else {
            lineBottom
        }
    }

    fun getLineTopWithoutPadding(layout: Layout, line: Int): Int {
        val top = layout.getLineTop(line)
        return if (line == 0) {
            top - layout.topPadding
        } else {
            top
        }
    }

    fun getLineHeight(layout: Layout, line: Int): Int {
        return layout.getLineTop(line + 1) - layout.getLineTop(line)
    }
}