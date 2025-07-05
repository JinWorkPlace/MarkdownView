package com.apps.markdown.sample.samples

import android.text.Spanned
import android.widget.Toast
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.LinkResolver
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.CoreProps
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.LinkSpan
import org.commonmark.node.Link
import java.util.Locale

@MarkwonSampleInfo(
    id = "20200629122230",
    title = "Obtain link title",
    description = "Obtain title (text) of clicked link, `[title](#destination)`",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.LINKS, Tag.SPAN]
)
class LinkTitleSample : MarkwonTextViewSample() {
    override fun render() {
        val md = "" + "# Links\n\n" + "[link title](#)"

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureSpansFactory(builder: io.noties.markwon.MarkwonSpansFactory.Builder) {
                builder.setFactory(
                    Link::class.java, object : SpanFactory {
                        override fun getSpans(
                            configuration: MarkwonConfiguration, props: RenderProps
                        ): Any {
                            return ClickSelfSpan(
                                configuration.theme(),
                                CoreProps.LINK_DESTINATION.require(props),
                                configuration.linkResolver()
                            )
                        }

                    })
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}

internal class ClickSelfSpan(
    theme: MarkwonTheme, link: String, resolver: LinkResolver
) : LinkSpan(theme, link, resolver) {
    override fun onClick(widget: android.view.View) {
        Toast.makeText(
            widget.context, String.format(
                Locale.ROOT, "clicked link title: '%s'", linkTitle(widget)
            ), Toast.LENGTH_LONG
        ).show()
        super.onClick(widget)
    }

    private fun linkTitle(widget: android.view.View): CharSequence? {
        if (widget !is android.widget.TextView) {
            return null
        }

        val spanned: Spanned = widget.text as Spanned
        val start: Int = spanned.getSpanStart(this)
        val end: Int = spanned.getSpanEnd(this)

        if (start < 0 || end < 0) {
            return null
        }

        return spanned.subSequence(start, end)
    }
}
