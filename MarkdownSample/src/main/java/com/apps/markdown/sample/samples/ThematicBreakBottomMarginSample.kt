package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.BlockHandlerDef
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.ThematicBreak

@MarkwonSampleInfo(
    id = "20200813154415",
    title = "Thematic break bottom margin",
    description = "Do not add a new line after thematic break (with the `BlockHandler`)",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.RENDERING]
)
class ThematicBreakBottomMarginSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Thematic break and margin\n\n" + "So, what if....\n\n" + "---\n\n" + "And **now**"

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                builder.blockHandler(object : BlockHandlerDef() {
                    override fun blockEnd(
                        visitor: MarkwonVisitor, node: org.commonmark.node.Node
                    ) {
                        if (visitor.hasNext(node)) {
                            visitor.ensureNewLine()

                            // thematic break won't have a new line
                            // similarly you can control other blocks
                            if (node !is ThematicBreak) {
                                visitor.forceNewLine()
                            }
                        }
                    }
                })
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}
