package io.noties.markwon

import io.noties.markwon.MarkwonVisitor.BlockHandler
import org.commonmark.node.Node

/**
 * @since 4.3.0
 */
class BlockHandlerDef : BlockHandler {
    override fun blockStart(visitor: MarkwonVisitor, node: Node) {
        visitor.ensureNewLine()
    }

    override fun blockEnd(visitor: MarkwonVisitor, node: Node) {
        if (visitor.hasNext(node)) {
            visitor.ensureNewLine()
            visitor.forceNewLine()
        }
    }
}
