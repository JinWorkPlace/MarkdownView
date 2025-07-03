package io.noties.markwon.core.spans

import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import io.noties.markwon.core.MarkwonTheme

/**
 * @since 3.0.0 split inline and block spans
 */
data class CodeSpan(
    private val theme: MarkwonTheme
) : MetricAffectingSpan() {
    override fun updateMeasureState(p: TextPaint) {
        apply(p)
    }

    override fun updateDrawState(ds: TextPaint) {
        apply(ds)
        ds.bgColor = theme.getCodeBackgroundColor(ds)
    }

    private fun apply(p: TextPaint) {
        theme.applyCodeTextStyle(p)
    }
}
