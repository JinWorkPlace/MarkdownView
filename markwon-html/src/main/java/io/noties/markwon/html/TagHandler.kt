package io.noties.markwon.html

import io.noties.markwon.MarkwonVisitor

abstract class TagHandler {
    abstract fun handle(
        visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, tag: HtmlTag
    )

    /**
     * @since 4.0.0
     */
    abstract fun supportedTags(): MutableCollection<String>


    companion object {
        @JvmStatic
        protected fun visitChildren(
            visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, block: HtmlTag.Block
        ) {
            var handler: TagHandler?

            for (child in block.children()) {
                if (!child.isClosed) {
                    continue
                }

                handler = renderer.tagHandler(child.name())
                if (handler != null) {
                    handler.handle(visitor, renderer, child)
                } else {
                    visitChildren(visitor, renderer, child)
                }
            }
        }
    }
}
