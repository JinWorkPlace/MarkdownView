package io.noties.markwon.core.spans

import android.text.Spannable
import android.text.Spanned
import android.widget.TextView
import java.lang.ref.WeakReference

/**
 * A special span that allows to obtain `TextView` in which spans are displayed
 *
 * @since 4.4.0
 */
class TextViewSpan(textView: TextView) {
    private val reference: WeakReference<TextView> = WeakReference<TextView>(textView)

    fun textView(): TextView? {
        return reference.get()
    }

    companion object {
        fun textViewOf(cs: CharSequence): TextView? {
            if (cs is Spanned) {
                return Companion.textViewOf(cs)
            }
            return null
        }

        @JvmStatic
        fun textViewOf(spanned: Spanned): TextView? {
            val spans = spanned.getSpans(0, spanned.length, TextViewSpan::class.java)
            return if (spans != null && spans.size > 0) spans[0]!!.textView()
            else null
        }

        @JvmStatic
        fun applyTo(spannable: Spannable, textView: TextView) {
            val spans =
                spannable.getSpans(0, spannable.length, TextViewSpan::class.java)
            if (spans != null) {
                for (span in spans) {
                    spannable.removeSpan(span)
                }
            }

            val span = TextViewSpan(textView)
            // `SPAN_INCLUSIVE_INCLUSIVE` to persist in case of possible text change (deletion, etc)
            spannable.setSpan(
                span, 0, spannable.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }
}
