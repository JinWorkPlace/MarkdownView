package io.noties.markwon.core

import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.Node

/**
 * A [MarkwonVisitor.NodeVisitor] that ensures that a markdown
 * block starts with a new line, all children are visited and if further content available
 * ensures a new line after self. Does not render any spans
 *
 * @since 3.0.0
 */
class SimpleBlockNodeVisitor : MarkwonVisitor.NodeVisitor<Node> {
    override fun visit(visitor: MarkwonVisitor, node: Node) {
        visitor.blockStart(node)

        // @since 3.0.1 we keep track of start in order to apply spans (optionally)
        val length = visitor.length()

        visitor.visitChildren(node)

        // @since 3.0.1 we apply optional spans
        visitor.setSpansForNodeOptional(node, length)

        visitor.blockEnd(node)
    }
}
