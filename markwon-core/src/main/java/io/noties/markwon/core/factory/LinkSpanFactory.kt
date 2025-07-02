package io.noties.markwon.core.factory

import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.CoreProps
import io.noties.markwon.core.spans.LinkSpan

class LinkSpanFactory : SpanFactory {
    override fun getSpans(configuration: MarkwonConfiguration, props: RenderProps): LinkSpan {
        return LinkSpan(
            theme = configuration.theme(),
            link = CoreProps.LINK_DESTINATION.require(props),
            resolver = configuration.linkResolver()
        )
    }
}
