package io.noties.markwon.core.spans

import android.text.TextPaint
import android.text.style.URLSpan
import android.view.View
import io.noties.markwon.LinkResolver
import io.noties.markwon.core.MarkwonTheme

class LinkSpan(
    private val theme: MarkwonTheme,
    /**
     * @since 4.2.0
     */
    val link: String,
    private val resolver: LinkResolver
) : URLSpan(link) {
    override fun onClick(widget: View) {
        resolver.resolve(widget, link)
    }

    override fun updateDrawState(ds: TextPaint) {
        theme.applyLinkStyle(ds)
    }
}
