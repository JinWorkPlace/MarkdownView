package io.noties.markwon.html

import io.noties.markwon.MarkwonVisitor

/**
 * @since 4.0.0
 */
class TagHandlerNoOp internal constructor(
    private val tags: MutableCollection<String>
) : TagHandler() {
    override fun handle(visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, tag: HtmlTag) {
        // no op
    }

    override fun supportedTags(): MutableCollection<String> {
        return tags
    }

    companion object {
        fun create(tag: String): TagHandlerNoOp {
            return TagHandlerNoOp(mutableSetOf(tag))
        }

        fun create(vararg tags: String): TagHandlerNoOp {
            return TagHandlerNoOp(mutableListOf(*tags))
        }
    }
}
