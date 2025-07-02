package io.noties.markwon

/**
 * @since 3.0.0
 */
interface SpanFactory {
    fun getSpans(configuration: MarkwonConfiguration, props: RenderProps): Any?
}
