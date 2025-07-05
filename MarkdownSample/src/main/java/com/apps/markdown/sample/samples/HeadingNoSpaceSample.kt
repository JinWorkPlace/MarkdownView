package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.core.CoreProps
import io.noties.markwon.core.MarkwonTheme
import org.commonmark.node.Heading

@MarkwonSampleInfo(
    id = "20200629125622",
    title = "Heading no padding",
    description = "Do not add a new line after heading node",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.SPACING, Tag.PADDING, Tag.SPACING, Tag.RENDERING]
)
class HeadingNoSpaceSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Title title title title title title title title title title" + "\n\ntext text text text" + ""

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureTheme(builder: MarkwonTheme.Builder) {
                builder.headingBreakHeight(0)
            }

            override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                builder.on(
                    Heading::class.java,
                    MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor, heading: Heading ->
                        visitor.ensureNewLine()
                        val length: Int = visitor.length()
                        visitor.visitChildren(heading)

                        CoreProps.HEADING_LEVEL.set(visitor.renderProps(), heading.level)

                        visitor.setSpansForNodeOptional(heading, length)
                        if (visitor.hasNext(heading)) {
                            visitor.ensureNewLine()
                        }
                    })
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}
