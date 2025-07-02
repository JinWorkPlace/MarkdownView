package io.noties.markwon.html.tag

import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.span.SubScriptSpan

class SubScriptHandler : SimpleTagHandler() {
    override fun getSpans(
        configuration: MarkwonConfiguration, renderProps: RenderProps, tag: HtmlTag
    ): Any? {
        return SubScriptSpan()
    }

    override fun supportedTags(): MutableCollection<String> {
        return mutableSetOf("sub")
    }
}
