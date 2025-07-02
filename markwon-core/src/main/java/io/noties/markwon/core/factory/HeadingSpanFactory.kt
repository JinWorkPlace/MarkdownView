package io.noties.markwon.core.factory

import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.CoreProps
import io.noties.markwon.core.spans.HeadingSpan

class HeadingSpanFactory : SpanFactory {
    override fun getSpans(configuration: MarkwonConfiguration, props: RenderProps): HeadingSpan {
        return HeadingSpan(
            theme = configuration.theme(),
            level = CoreProps.HEADING_LEVEL.require(props)
        )
    }
}
