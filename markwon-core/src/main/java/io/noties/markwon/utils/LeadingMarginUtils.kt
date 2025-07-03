package io.noties.markwon.utils

import android.text.Spanned

object LeadingMarginUtils {
    fun selfStart(start: Int, text: CharSequence?, span: Any?): Boolean {
        return text is Spanned && text.getSpanStart(span) == start
    }

    fun selfEnd(end: Int, text: CharSequence?, span: Any?): Boolean {
        return text is Spanned && text.getSpanEnd(span) == end
    }
}
