package io.noties.markwon.editor

import android.text.TextPaint
import android.text.style.CharacterStyle
import io.noties.markwon.utils.ColorUtils

internal class PunctuationSpan : CharacterStyle() {
    override fun updateDrawState(tp: TextPaint) {
        val color = ColorUtils.applyAlpha(tp.color, DEF_PUNCTUATION_ALPHA)
        tp.setColor(color)
    }

    companion object {
        private const val DEF_PUNCTUATION_ALPHA = 75
    }
}
