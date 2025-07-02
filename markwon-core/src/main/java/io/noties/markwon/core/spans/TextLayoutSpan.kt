package io.noties.markwon.core.spans

import android.text.Layout
import android.text.Spannable
import android.text.Spanned
import java.lang.ref.WeakReference

/**
 * @since 4.4.0
 */
class TextLayoutSpan internal constructor(layout: Layout) {
    private val reference: WeakReference<Layout> = WeakReference<Layout>(layout)

    fun layout(): Layout? {
        return reference.get()
    }

    companion object {
        /**
         * @see .applyTo
         */
        fun layoutOf(cs: CharSequence): Layout? {
            if (cs is Spanned) {
                return Companion.layoutOf(cs)
            }
            return null
        }

        @JvmStatic
        fun layoutOf(spanned: Spanned): Layout? {
            val spans = spanned.getSpans(
                0, spanned.length, TextLayoutSpan::class.java
            )
            return if (spans != null && spans.size > 0) spans[0]!!.layout()
            else null
        }

        fun applyTo(spannable: Spannable, layout: Layout) {
            // remove all current ones (only one should be present)

            val spans = spannable.getSpans(0, spannable.length, TextLayoutSpan::class.java)
            if (spans != null) {
                for (span in spans) {
                    spannable.removeSpan(span)
                }
            }

            val span = TextLayoutSpan(layout)
            spannable.setSpan(
                span, 0, spannable.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }
}
