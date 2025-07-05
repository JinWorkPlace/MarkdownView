package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.BlockHandlerDef
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.Node

@MarkwonSampleInfo(
    id = "20200629130227",
    title = "All blocks no padding",
    description = "Do not render new lines (padding) after all blocks",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.BLOCK, Tag.SPACING, Tag.PADDING, Tag.RENDERING]
)
class AllBlocksNoForcedNewLineSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Hello there!\n\n" + "* a first\n" + "* second\n" + "- third\n" + "* * nested one\n\n" + "> block quote\n\n" + "> > and nested one\n\n" + "```java\n" + "final int i = 0;\n" + "```\n\n"

        // extend default block handler
        val blockHandler: MarkwonVisitor.BlockHandler = object : BlockHandlerDef() {
            public override fun blockEnd(visitor: MarkwonVisitor, node: Node) {
                if (visitor.hasNext(node)) {
                    visitor.ensureNewLine()
                }
            }
        }

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                builder.blockHandler(blockHandler)
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}
