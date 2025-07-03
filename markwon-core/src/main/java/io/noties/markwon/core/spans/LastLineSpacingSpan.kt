package io.noties.markwon.core.spans

import android.graphics.Paint.FontMetricsInt
import android.text.Spanned
import android.text.style.LineHeightSpan
import androidx.annotation.Px

/**
 * @since 4.0.0
 */
data class LastLineSpacingSpan(
    @param:Px private val spacing: Int
) : LineHeightSpan {
    override fun chooseHeight(
        text: CharSequence, start: Int, end: Int, spanstartv: Int, v: Int, fm: FontMetricsInt
    ) {
        if (selfEnd(end, text, this)) {
            // let's just add what we want
            fm.descent += spacing
            fm.bottom += spacing
        }
    }

    companion object {
        fun create(@Px spacing: Int): LastLineSpacingSpan {
            return LastLineSpacingSpan(spacing)
        }

        private fun selfEnd(end: Int, text: CharSequence, span: Any?): Boolean {
            // this is some kind of interesting magic here... only the last
            // span will receive correct _end_ argument, but previous spans
            // receive it tilted by one (1). Most likely it's just a new-line character... and
            // if needed we could check for that
            val spanEnd = (text as Spanned).getSpanEnd(span)
            return spanEnd == end || spanEnd == end - 1
        }
    }
}
