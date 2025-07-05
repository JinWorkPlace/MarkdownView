package com.apps.markdown.sample.samples.plugins

import android.widget.TextView
import com.apps.markdown.sample.R
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.samples.plugins.shared.AnchorHeadingPlugin
import com.apps.markdown.sample.samples.plugins.shared.AnchorHeadingPlugin.ScrollTo
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.core.SimpleBlockNodeVisitor
import org.commonmark.node.BulletList
import org.commonmark.node.CustomBlock
import org.commonmark.node.Heading

@MarkwonSampleInfo(
    id = "20200629161226",
    title = "Table of contents",
    description = "Sample plugin that adds a table of contents header",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.RENDERING, Tag.PLUGIN]
)
class TableOfContentsSample : MarkwonTextViewSample() {
    override fun render() {
        val lorem: String = context.getString(R.string.lorem)
        val md =
            "# First\n$lorem\n\n# Second\n$lorem\n\n## Second level\n\n$lorem\n\n### Level 3\n\n$lorem\n\n# First again\n$lorem\n\n"

        val markwon: Markwon = Markwon.builder(context).usePlugin(TableOfContentsPlugin())
            .usePlugin(AnchorHeadingPlugin(object : ScrollTo {
                override fun scrollTo(view: TextView, top: Int) {
                    scrollView.smoothScrollTo(0, top)
                }
            })).build()

        markwon.setMarkdown(textView, md)
    }
}

internal class TableOfContentsPlugin : AbstractMarkwonPlugin() {
    override fun configure(registry: MarkwonPlugin.Registry) {
        // just to make it explicit
        registry.require(AnchorHeadingPlugin::class.java)
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        builder.on<TableOfContentsBlock>(TableOfContentsBlock::class.java, SimpleBlockNodeVisitor())
    }

    override fun beforeRender(node: org.commonmark.node.Node) {
        // custom block to hold TOC

        val block = TableOfContentsBlock()

        // create TOC title
        run {
            val text = org.commonmark.node.Text("Table of contents")
            val heading: Heading = Heading()
            // important one - set TOC heading level
            heading.level = 1
            heading.appendChild(text)
            block.appendChild(heading)
        }

        val visitor = HeadingVisitor(block)
        node.accept(visitor)

        // make it the very first node in rendered markdown
        node.prependChild(block)
    }

    private class HeadingVisitor(node: org.commonmark.node.Node) :
        org.commonmark.node.AbstractVisitor() {
        private val bulletList: BulletList = BulletList()
        private val builder = java.lang.StringBuilder()
        private var isInsideHeading = false

        init {
            node.appendChild(bulletList)
        }

        override fun visit(heading: Heading) {
            this.isInsideHeading = true
            try {
                // reset build from previous content
                builder.setLength(0)

                // obtain level (can additionally filter by level, to skip lower ones)
                val level: Int = heading.level

                // build heading title
                visitChildren(heading)

                // initial list item
                val listItem = org.commonmark.node.ListItem()

                var parent: org.commonmark.node.Node = listItem
                var node: org.commonmark.node.Node = listItem

                (1..<level).forEach { i ->
                    val li = org.commonmark.node.ListItem()
                    val bulletList = BulletList()
                    bulletList.appendChild(li)
                    parent.appendChild(bulletList)
                    parent = li
                    node = li
                }

                val content = builder.toString()
                val link =
                    org.commonmark.node.Link("#" + AnchorHeadingPlugin.createAnchor(content), null)
                val text = org.commonmark.node.Text(content)
                link.appendChild(text)
                node.appendChild(link)
                bulletList.appendChild(listItem)
            } finally {
                isInsideHeading = false
            }
        }

        override fun visit(text: org.commonmark.node.Text) {
            // can additionally check if we are building heading (to skip all other texts)
            if (isInsideHeading) {
                builder.append(text.literal)
            }
        }
    }

    private class TableOfContentsBlock : CustomBlock()
}
