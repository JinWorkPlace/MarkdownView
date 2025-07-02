package io.noties.markwon.core.spans

import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class StrongEmphasisSpan : MetricAffectingSpan() {
    override fun updateMeasureState(p: TextPaint) {
        p.isFakeBoldText = true
    }

    override fun updateDrawState(tp: TextPaint) {
        tp.isFakeBoldText = true
    }
}
