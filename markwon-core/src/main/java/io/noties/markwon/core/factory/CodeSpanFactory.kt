package io.noties.markwon.core.factory

import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.spans.CodeSpan

class CodeSpanFactory : SpanFactory {
    override fun getSpans(configuration: MarkwonConfiguration, props: RenderProps): CodeSpan {
        return CodeSpan(configuration.theme())
    }
}
