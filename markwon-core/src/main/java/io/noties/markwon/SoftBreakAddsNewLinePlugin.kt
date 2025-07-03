package io.noties.markwon

import org.commonmark.node.SoftLineBreak

/**
 * @since 4.3.0
 */
class SoftBreakAddsNewLinePlugin : AbstractMarkwonPlugin() {
    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(
            SoftLineBreak::class.java,
            MarkwonVisitor.NodeVisitor { visitor, softLineBreak -> visitor.ensureNewLine() })
    }

    companion object {
        fun create(): SoftBreakAddsNewLinePlugin {
            return SoftBreakAddsNewLinePlugin()
        }
    }
}
