package io.noties.markwon.html.tag

import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.MarkwonHtmlRenderer
import io.noties.markwon.html.TagHandler
import org.commonmark.node.BlockQuote

class BlockquoteHandler : TagHandler() {
    override fun handle(
        visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, tag: HtmlTag
    ) {
        if (tag.isBlock) {
            visitChildren(visitor, renderer, tag.asBlock)
        }

        val configuration = visitor.configuration()
        val factory = configuration.spansFactory().get(BlockQuote::class.java)
        if (factory != null) {
            SpannableBuilder.setSpans(
                visitor.builder(),
                factory.getSpans(configuration, visitor.renderProps()),
                tag.start(),
                tag.end()
            )
        }
    }

    override fun supportedTags(): MutableCollection<String> {
        return mutableSetOf("blockquote")
    }
}
