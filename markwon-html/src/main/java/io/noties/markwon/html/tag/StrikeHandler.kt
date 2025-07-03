package io.noties.markwon.html.tag

import android.text.style.StrikethroughSpan
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.MarkwonHtmlRenderer
import io.noties.markwon.html.TagHandler
import org.commonmark.ext.gfm.strikethrough.Strikethrough

class StrikeHandler : TagHandler() {
    override fun handle(
        visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, tag: HtmlTag
    ) {
        if (tag.isBlock) {
            visitChildren(visitor, renderer, tag.asBlock)
        }

        SpannableBuilder.setSpans(
            visitor.builder(),
            if (HAS_MARKDOWN_IMPLEMENTATION) getMarkdownSpans(visitor) else StrikethroughSpan(),
            tag.start(),
            tag.end()
        )
    }

    override fun supportedTags(): MutableCollection<String> {
        return mutableListOf("s", "del")
    }

    companion object {
        // flag to detect if commonmark-java-strikethrough is in classpath, so we use SpanFactory
        // to obtain strikethrough span
        private val HAS_MARKDOWN_IMPLEMENTATION: Boolean

        init {
            var hasMarkdownImplementation: Boolean
            try {
                // @since 4.3.1 we class Class.forName instead of trying
                //  to access the class by full qualified name (which caused issues with DexGuard)
                Class.forName("org.commonmark.ext.gfm.strikethrough.Strikethrough")
                hasMarkdownImplementation = true
            } catch (_: Throwable) {
                hasMarkdownImplementation = false
            }
            HAS_MARKDOWN_IMPLEMENTATION = hasMarkdownImplementation
        }

        private fun getMarkdownSpans(visitor: MarkwonVisitor): Any? {
            val configuration = visitor.configuration()
            val spanFactory = configuration.spansFactory().get(Strikethrough::class.java)
            if (spanFactory == null) {
                return null
            }
            return spanFactory.getSpans(configuration, visitor.renderProps())
        }
    }
}
