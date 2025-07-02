package io.noties.markwon.html

import io.noties.markwon.MarkwonVisitor

internal class MarkwonHtmlRendererNoOp : MarkwonHtmlRenderer() {
    override fun render(visitor: MarkwonVisitor, parser: MarkwonHtmlParser) {
        parser.reset()
    }

    override fun tagHandler(tagName: String): TagHandler? {
        return null
    }
}
