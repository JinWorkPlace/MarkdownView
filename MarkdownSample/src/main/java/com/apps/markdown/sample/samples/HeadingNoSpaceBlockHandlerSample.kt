package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.BlockHandlerDef
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.Heading

@MarkwonSampleInfo(
    id = "20200629125924",
    title = "Heading no padding (block handler)",
    description = "Process padding (spacing) after heading with a " + "`BlockHandler`",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.BLOCK, Tag.SPACING, Tag.PADDING, Tag.HEADING, Tag.RENDERING]
)
class HeadingNoSpaceBlockHandlerSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Title title title title title title title title title title\n\n" + "text text text text" + ""

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                builder.blockHandler(object : BlockHandlerDef() {
                    override fun blockEnd(
                        visitor: MarkwonVisitor, node: org.commonmark.node.Node
                    ) {
                        if (node is Heading) {
                            if (visitor.hasNext(node)) {
                                visitor.ensureNewLine()
                                // ensure new line but do not force insert one
                            }
                        } else {
                            super.blockEnd(visitor, node)
                        }
                    }
                })
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}
