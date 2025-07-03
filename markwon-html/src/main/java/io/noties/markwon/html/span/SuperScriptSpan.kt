package io.noties.markwon.html.span

import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import io.noties.markwon.html.HtmlPlugin

class SuperScriptSpan : MetricAffectingSpan() {
    override fun updateDrawState(tp: TextPaint) {
        apply(tp)
    }

    override fun updateMeasureState(tp: TextPaint) {
        apply(tp)
    }

    private fun apply(paint: TextPaint) {
        paint.textSize = paint.textSize * HtmlPlugin.SCRIPT_DEF_TEXT_SIZE_RATIO
        paint.baselineShift += (paint.ascent() / 2).toInt()
    }
}
