package io.noties.markwon.html.tag

import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.span.SuperScriptSpan

class SuperScriptHandler : SimpleTagHandler() {
    override fun getSpans(
        configuration: MarkwonConfiguration, renderProps: RenderProps, tag: HtmlTag
    ): Any? {
        return SuperScriptSpan()
    }

    override fun supportedTags(): MutableCollection<String> {
        return mutableSetOf("sup")
    }
}
