package io.noties.markwon.utils

import android.text.Spannable
import android.text.SpannableString

object NoCopySpannableFactory : Spannable.Factory() {
    override fun newSpannable(source: CharSequence?): Spannable {
        return source as? Spannable ?: SpannableString(source)
    }
}