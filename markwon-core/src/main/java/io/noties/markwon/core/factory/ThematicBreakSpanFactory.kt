package io.noties.markwon.core.factory

import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.spans.ThematicBreakSpan

class ThematicBreakSpanFactory : SpanFactory {
    override fun getSpans(
        configuration: MarkwonConfiguration, props: RenderProps
    ): ThematicBreakSpan {
        return ThematicBreakSpan(configuration.theme())
    }
}
