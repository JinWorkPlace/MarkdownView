package com.apps.markdown.sample.samples

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory

import org.commonmark.node.Paragraph

@MarkwonSampleInfo(
    id = "20200629122647",
    title = "Paragraph style",
    description = "Apply a style (via span) to a paragraph",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.PARAGRAPH, Tag.STYLE, Tag.SPAN]
)
class ParagraphSpanStyle : MarkwonTextViewSample() {
    override fun render() {
        val md = "# Hello!\n\nA paragraph?\n\nIt should be!"

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                // apply a span to a Paragraph
                builder.setFactory<Paragraph>(
                    Paragraph::class.java, object : SpanFactory {
                        override fun getSpans(
                            configuration: MarkwonConfiguration, props: RenderProps
                        ): Any {
                            return ForegroundColorSpan(Color.GREEN)
                        }
                    })
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}
