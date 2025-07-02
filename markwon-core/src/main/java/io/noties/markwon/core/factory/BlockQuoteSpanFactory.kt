package io.noties.markwon.core.factory

import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.spans.BlockQuoteSpan

class BlockQuoteSpanFactory : SpanFactory {
    override fun getSpans(configuration: MarkwonConfiguration, props: RenderProps): BlockQuoteSpan {
        return BlockQuoteSpan(configuration.theme())
    }
}
