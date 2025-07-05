package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.Heading

@MarkwonSampleInfo(
    id = "20200629123308",
    title = "Disable node from rendering",
    description = "Disable _parsed_ node from being rendered (markdown syntax is still consumed)",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.PARSING, Tag.RENDERING]
)
class DisableNodeSample : MarkwonTextViewSample() {
    override fun render() {
        val md = "# Heading 1\n\n## Heading 2\n\n**other** content [here](#)"

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                builder.on(Heading::class.java, null)
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}
