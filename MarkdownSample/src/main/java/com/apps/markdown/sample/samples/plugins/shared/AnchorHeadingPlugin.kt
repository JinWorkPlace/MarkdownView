package com.apps.markdown.sample.samples.plugins.shared

import android.text.Spannable
import android.text.Spanned
import android.view.View
import android.widget.TextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.LinkResolverDef
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.spans.HeadingSpan
import java.util.Locale

class AnchorHeadingPlugin(
    private val scrollTo: ScrollTo
) : AbstractMarkwonPlugin() {
    interface ScrollTo {
        fun scrollTo(view: TextView, top: Int)
    }

    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
        builder.linkResolver(AnchorLinkResolver(scrollTo))
    }

    override fun afterSetText(textView: TextView) {
        val spannable = textView.text as Spannable
        // obtain heading spans
        val spans = spannable.getSpans(0, spannable.length, HeadingSpan::class.java)
        if (spans != null) {
            for (span in spans) {
                val start = spannable.getSpanStart(span)
                val end = spannable.getSpanEnd(span)
                val flags = spannable.getSpanFlags(span)
                spannable.setSpan(
                    AnchorSpan(createAnchor(spannable.subSequence(start, end))), start, end, flags
                )
            }
        }
    }

    private class AnchorLinkResolver(private val scrollTo: ScrollTo) : LinkResolverDef() {
        override fun resolve(view: View, link: String) {
            if (link.startsWith("#")) {
                val textView = view as TextView
                val spanned: Spanned = textView.text as Spannable
                val spans: Array<AnchorSpan>? =
                    spanned.getSpans(0, spanned.length, AnchorSpan::class.java)
                if (spans != null) {
                    val anchor = link.substring(1)
                    for (span in spans) {
                        if (anchor == span.anchor) {
                            val start = spanned.getSpanStart(span)
                            val line = textView.layout.getLineForOffset(start)
                            val top = textView.layout.getLineTop(line)
                            scrollTo.scrollTo(textView, top)
                            return
                        }
                    }
                }
            }
            super.resolve(view, link)
        }
    }

    private class AnchorSpan(val anchor: String)

    companion object {
        fun createAnchor(content: CharSequence): String {
            return content.toString().replace("\\W".toRegex(), "").lowercase(Locale.getDefault())
        }
    }
}
