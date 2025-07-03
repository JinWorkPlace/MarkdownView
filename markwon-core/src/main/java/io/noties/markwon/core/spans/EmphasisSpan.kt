package io.noties.markwon.core.spans

import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class EmphasisSpan : MetricAffectingSpan() {
    override fun updateMeasureState(p: TextPaint) {
        p.textSkewX = -0.25f
    }

    override fun updateDrawState(tp: TextPaint) {
        tp.textSkewX = -0.25f
    }
}
