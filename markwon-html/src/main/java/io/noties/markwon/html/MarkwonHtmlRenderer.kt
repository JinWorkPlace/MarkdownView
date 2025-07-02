package io.noties.markwon.html

import io.noties.markwon.MarkwonVisitor

/**
 * @since 2.0.0
 */
abstract class MarkwonHtmlRenderer {
    abstract fun render(
        visitor: MarkwonVisitor, parser: MarkwonHtmlParser
    )

    abstract fun tagHandler(tagName: String): TagHandler?
}
