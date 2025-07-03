package io.noties.markwon.core.factory

import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.spans.StrongEmphasisSpan

class StrongEmphasisSpanFactory : SpanFactory {
    override fun getSpans(
        configuration: MarkwonConfiguration, props: RenderProps
    ): StrongEmphasisSpan {
        return StrongEmphasisSpan()
    }
}
