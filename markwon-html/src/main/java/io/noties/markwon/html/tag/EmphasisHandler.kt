package io.noties.markwon.html.tag

import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.html.HtmlTag
import org.commonmark.node.Emphasis

class EmphasisHandler : SimpleTagHandler() {
    override fun getSpans(
        configuration: MarkwonConfiguration, renderProps: RenderProps, tag: HtmlTag
    ): Any? {
        val spanFactory = configuration.spansFactory().get(Emphasis::class.java)
        if (spanFactory == null) {
            return null
        }
        return spanFactory.getSpans(configuration, renderProps)
    }

    override fun supportedTags(): MutableCollection<String> {
        return mutableListOf("i", "em", "cite", "dfn")
    }
}
