package io.noties.markwon.utils

import android.text.Spannable
import android.text.SpannableString

class NoCopySpannableFactory : Spannable.Factory() {

    companion object {
        fun getInstance(): NoCopySpannableFactory = Holder.INSTANCE
    }

    override fun newSpannable(source: CharSequence?): Spannable {
        return source as? Spannable ?: SpannableString(source)
    }

    private object Holder {
        val INSTANCE = NoCopySpannableFactory()
    }
}