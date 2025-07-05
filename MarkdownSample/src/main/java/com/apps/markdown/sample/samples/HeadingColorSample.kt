package com.apps.markdown.sample.samples

import android.text.style.ForegroundColorSpan
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.CoreProps
import org.commonmark.node.Heading

@MarkwonSampleInfo(
    id = "20201203224611",
    title = "Color of heading",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.RENDERING]
)
class HeadingColorSample : MarkwonTextViewSample() {
    override fun render() {
        val md = "" + "# Heading 1\n" + "## Heading 2\n" + "### Heading 3\n" + "#### Heading 4"

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureSpansFactory(builder: io.noties.markwon.MarkwonSpansFactory.Builder) {
                builder.appendFactory(
                    Heading::class.java,
                    object : SpanFactory {
                        override fun getSpans(
                            configuration: MarkwonConfiguration, props: RenderProps
                        ): Any {
                            val level: Int = CoreProps.HEADING_LEVEL.require(props)
                            val color: Int = when (level) {
                                1 -> {
                                    android.graphics.Color.RED
                                }

                                2 -> {
                                    android.graphics.Color.GREEN
                                }

                                else -> {
                                    android.graphics.Color.BLUE
                                }
                            }
                            return ForegroundColorSpan(color)
                        }
                    },
                )
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}
