package io.noties.markwon.utils

import android.graphics.Canvas
import android.text.Spanned
import io.noties.markwon.core.spans.TextLayoutSpan
import io.noties.markwon.core.spans.TextViewSpan

/**
 * @since 4.4.0
 */
object SpanUtils {
    fun width(canvas: Canvas, cs: CharSequence): Int {
        // Layout
        // TextView
        // canvas

        if (cs is Spanned) {
            val spanned = cs

            // if we are displayed with layout information -> use it
            val layout = TextLayoutSpan.layoutOf(spanned)
            if (layout != null) {
                return layout.width
            }

            // if we have TextView -> obtain width from it (exclude padding)
            val textView = TextViewSpan.textViewOf(spanned)
            if (textView != null) {
                return textView.width - textView.paddingLeft - textView.paddingRight
            }
        }

        // else just use canvas width
        return canvas.width
    }
}
