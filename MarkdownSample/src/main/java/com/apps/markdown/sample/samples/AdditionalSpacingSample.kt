package com.apps.markdown.sample.samples

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
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.LastLineSpacingSpan
import org.commonmark.node.Heading

@MarkwonSampleInfo(
    id = "20200629125321",
    title = "Additional spacing after block",
    description = "Add additional spacing (padding) after last line of a block",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.SPACING, Tag.PADDING, Tag.SPAN]
)
class AdditionalSpacingSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Title title title title title title title title title title \n\ntext text text text"

        // please note that bottom line (after 1 & 2 levels) will be drawn _AFTER_ padding
        val spacing = (128 * context.resources.displayMetrics.density + .5f).toInt()

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureTheme(builder: MarkwonTheme.Builder) {
                builder.headingBreakHeight(0)
            }

            override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                builder.appendFactory(
                    Heading::class.java, object : SpanFactory {
                        override fun getSpans(
                            configuration: MarkwonConfiguration, props: RenderProps
                        ): Any {
                            return LastLineSpacingSpan(spacing)
                        }
                    })
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}
