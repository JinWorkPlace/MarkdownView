package io.noties.markwon

/**
 * @since 4.1.1
 */
internal abstract class MarkwonVisitorFactory {
    abstract fun create(): MarkwonVisitor

    companion object {
        fun create(
            builder: MarkwonVisitor.Builder, configuration: MarkwonConfiguration
        ): MarkwonVisitorFactory {
            return object : MarkwonVisitorFactory() {
                override fun create(): MarkwonVisitor {
                    return builder.build(configuration, RenderPropsImpl())
                }
            }
        }
    }
}
