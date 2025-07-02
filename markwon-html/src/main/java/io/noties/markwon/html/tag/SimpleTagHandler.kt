package io.noties.markwon.html.tag

import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.RenderProps
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.MarkwonHtmlRenderer
import io.noties.markwon.html.TagHandler

abstract class SimpleTagHandler : TagHandler() {
    abstract fun getSpans(
        configuration: MarkwonConfiguration, renderProps: RenderProps, tag: HtmlTag
    ): Any?

    abstract override fun supportedTags(): MutableCollection<String>


    override fun handle(visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, tag: HtmlTag) {
        // @since 4.5.0 check if tag is block one and visit children
        if (tag.isBlock) {
            visitChildren(visitor, renderer, tag.asBlock)
        }

        val spans = getSpans(visitor.configuration(), visitor.renderProps(), tag)
        if (spans != null) {
            SpannableBuilder.setSpans(visitor.builder(), spans, tag.start(), tag.end())
        }
    }
}
