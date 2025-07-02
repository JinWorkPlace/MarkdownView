package io.noties.markwon.html.tag

import android.text.style.UnderlineSpan
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.MarkwonHtmlRenderer
import io.noties.markwon.html.TagHandler

class UnderlineHandler : TagHandler() {
    override fun handle(
        visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, tag: HtmlTag
    ) {
        // as parser doesn't treat U tag as an inline one,
        // thus doesn't allow children, we must visit them first

        if (tag.isBlock) {
            visitChildren(visitor, renderer, tag.asBlock)
        }

        SpannableBuilder.setSpans(
            visitor.builder(), UnderlineSpan(), tag.start(), tag.end()
        )
    }

    override fun supportedTags(): MutableCollection<String> {
        return mutableListOf("u", "ins")
    }
}
