package io.noties.markwon

import io.noties.markwon.MarkwonVisitor.BlockHandler
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.CustomBlock
import org.commonmark.node.CustomNode
import org.commonmark.node.Document
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.LinkReferenceDefinition
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import java.util.Collections

/**
 * @since 3.0.0
 */
internal open class MarkwonVisitorImpl(
    private val configuration: MarkwonConfiguration,
    private val renderProps: RenderProps,
    private val builder: SpannableBuilder,
    private val nodes: MutableMap<Class<out Node>, MarkwonVisitor.NodeVisitor<out Node>>, // @since 4.3.0
    private val blockHandler: BlockHandler
) : MarkwonVisitor {
    override fun visit(blockQuote: BlockQuote) {
        visit(blockQuote as Node)
    }

    override fun visit(bulletList: BulletList) {
        visit(bulletList as Node)
    }

    override fun visit(code: Code) {
        visit(code as Node)
    }

    override fun visit(document: Document) {
        visit(document as Node)
    }

    override fun visit(emphasis: Emphasis) {
        visit(emphasis as Node)
    }

    override fun visit(fencedCodeBlock: FencedCodeBlock) {
        visit(fencedCodeBlock as Node)
    }

    override fun visit(hardLineBreak: HardLineBreak?) {
        visit(hardLineBreak as Node)
    }

    override fun visit(heading: Heading) {
        visit(heading as Node)
    }

    override fun visit(thematicBreak: ThematicBreak) {
        visit(thematicBreak as Node)
    }

    override fun visit(htmlInline: HtmlInline) {
        visit(htmlInline as Node)
    }

    override fun visit(htmlBlock: HtmlBlock) {
        visit(htmlBlock as Node)
    }

    override fun visit(image: Image) {
        visit(image as Node)
    }

    override fun visit(indentedCodeBlock: IndentedCodeBlock) {
        visit(indentedCodeBlock as Node)
    }

    override fun visit(link: Link) {
        visit(link as Node)
    }

    override fun visit(listItem: ListItem) {
        visit(listItem as Node)
    }

    override fun visit(orderedList: OrderedList) {
        visit(orderedList as Node)
    }

    override fun visit(paragraph: Paragraph) {
        visit(paragraph as Node)
    }

    override fun visit(softLineBreak: SoftLineBreak) {
        visit(softLineBreak as Node)
    }

    override fun visit(strongEmphasis: StrongEmphasis) {
        visit(strongEmphasis as Node)
    }

    override fun visit(text: Text) {
        visit(text as Node)
    }

    override fun visit(linkReferenceDefinition: LinkReferenceDefinition) {
        visit(linkReferenceDefinition as Node)
    }

    override fun visit(customBlock: CustomBlock) {
        visit(customBlock as Node)
    }

    override fun visit(customNode: CustomNode) {
        visit(customNode as Node)
    }

    private fun visit(node: Node) {
        val nodeVisitor = nodes[node.javaClass] as MarkwonVisitor.NodeVisitor<Node>?
        if (nodeVisitor != null) {
            nodeVisitor.visit(this, node)
        } else {
            visitChildren(node)
        }
    }

    override fun configuration(): MarkwonConfiguration {
        return configuration
    }

    override fun renderProps(): RenderProps {
        return renderProps
    }

    override fun builder(): SpannableBuilder {
        return builder
    }

    override fun visitChildren(node: Node) {
        var firstChild = node.firstChild
        while (firstChild != null) {
            // A subclass of this visitor might modify the node, resulting in getNext returning a different node or no
            // node after visiting it. So get the next node before visiting.
            val next = firstChild.next
            firstChild.accept(this)
            firstChild = next
        }
    }

    override fun hasNext(node: Node): Boolean {
        return node.next != null
    }

    override fun ensureNewLine() {
        if (builder.isNotEmpty() && '\n' != builder.lastChar()) {
            builder.append('\n')
        }
    }

    override fun forceNewLine() {
        builder.append('\n')
    }

    override fun length(): Int {
        return builder.length
    }

    override fun setSpans(start: Int, spans: Any?) {
        SpannableBuilder.setSpans(builder, spans, start, builder.length)
    }

    override fun clear() {
        renderProps.clearAll()
        builder.clear()
    }

    override fun <N : Node> setSpansForNode(node: N, start: Int) {
        setSpansForNode(node.javaClass, start)
    }

    override fun <N : Node> setSpansForNode(node: Class<N>, start: Int) {
        setSpans(
            start, configuration.spansFactory().require(node).getSpans(configuration, renderProps)
        )
    }

    override fun <N : Node> setSpansForNodeOptional(node: N, start: Int) {
        setSpansForNodeOptional(node.javaClass, start)
    }

    override fun <N : Node> setSpansForNodeOptional(node: Class<N>, start: Int) {
        val factory = configuration.spansFactory().get(node)
        if (factory != null) {
            setSpans(start, factory.getSpans(configuration, renderProps))
        }
    }

    override fun blockStart(node: Node) {
        blockHandler.blockStart(this, node)
    }

    override fun blockEnd(node: Node) {
        blockHandler.blockEnd(this, node)
    }

    internal class BuilderImpl : MarkwonVisitor.Builder {
        private val nodes: MutableMap<Class<out Node>, MarkwonVisitor.NodeVisitor<out Node>> =
            HashMap()
        private var blockHandler: BlockHandler? = null

        override fun <N : Node> on(
            node: Class<N>, nodeVisitor: MarkwonVisitor.NodeVisitor<in N>?
        ): MarkwonVisitor.Builder {
            // @since 4.1.1 we might actually introduce a local flag to check if it's been built
            //  and throw an exception here if some modification is requested
            //  NB, as we might be built from different threads this flag must be synchronized

            // we should allow `null` to exclude node from being visited (for example to disable
            // some functionality)

            if (nodeVisitor == null) {
                nodes.remove(node)
            } else {
                nodes.put(node, nodeVisitor)
            }
            return this
        }

        override fun blockHandler(blockHandler: BlockHandler): MarkwonVisitor.Builder {
            this.blockHandler = blockHandler
            return this
        }

        override fun build(
            configuration: MarkwonConfiguration, renderProps: RenderProps
        ): MarkwonVisitor {
            // @since 4.3.0
            var blockHandler = this.blockHandler
            if (blockHandler == null) {
                blockHandler = BlockHandlerDef()
            }

            return MarkwonVisitorImpl(
                configuration,
                renderProps,
                SpannableBuilder(),
                Collections.unmodifiableMap(nodes),
                blockHandler
            )
        }
    }
}
