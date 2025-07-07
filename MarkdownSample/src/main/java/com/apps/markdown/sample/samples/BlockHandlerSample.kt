package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.Node

@MarkwonSampleInfo(
    id = "20200729090524",
    title = "Block handler",
    description = "Custom block delimiters that control new lines after block nodes",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.RENDERING]
)
class BlockHandlerSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Heading\n" + "* one\n" + "* two\n" + "* three\n" + "---\n" + "> a quote\n\n" + "```\n" + "code\n" + "```\n" + "some text after"

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                builder.blockHandler(BlockHandlerNoAdditionalNewLines())
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}

internal class BlockHandlerNoAdditionalNewLines : MarkwonVisitor.BlockHandler {
    override fun blockStart(visitor: MarkwonVisitor, node: Node) {
        visitor.ensureNewLine()
    }

    override fun blockEnd(visitor: MarkwonVisitor, node: Node) {
        if (visitor.hasNext(node)) {
            visitor.ensureNewLine()
        }
    }
}
