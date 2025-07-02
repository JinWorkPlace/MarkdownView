package io.noties.markwon.simple.ext

import io.noties.markwon.SpanFactory
import org.commonmark.node.CustomNode
import org.commonmark.node.Visitor

// @since 4.0.0
internal class SimpleExtNode(private val spanFactory: SpanFactory) : CustomNode() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    fun spanFactory(): SpanFactory {
        return spanFactory
    }
}
