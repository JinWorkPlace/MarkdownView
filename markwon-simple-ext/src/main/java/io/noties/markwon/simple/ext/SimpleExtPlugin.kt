package io.noties.markwon.simple.ext

import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpanFactory
import io.noties.markwon.SpannableBuilder
import org.commonmark.parser.Parser

/**
 * @since 4.0.0
 */
class SimpleExtPlugin internal constructor() : AbstractMarkwonPlugin() {
    interface SimpleExtConfigure {
        fun configure(plugin: SimpleExtPlugin)
    }

    private val builder = SimpleExtBuilder()

    fun addExtension(length: Int, character: Char, spanFactory: SpanFactory): SimpleExtPlugin {
        builder.addExtension(length, character, spanFactory)
        return this
    }

    fun addExtension(
        length: Int,
        openingCharacter: Char,
        closingCharacter: Char,
        spanFactory: SpanFactory
    ): SimpleExtPlugin {
        builder.addExtension(length, openingCharacter, closingCharacter, spanFactory)
        return this
    }

    override fun configureParser(builder: Parser.Builder) {
        for (processor in this.builder.build()) {
            builder.customDelimiterProcessor(processor)
        }
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(
            SimpleExtNode::class.java,
            MarkwonVisitor.NodeVisitor { visitor, simpleExtNode ->
                val length = visitor.length()

                visitor.visitChildren(simpleExtNode)

                SpannableBuilder.setSpans(
                    visitor.builder(),
                    simpleExtNode.spanFactory()
                        .getSpans(visitor.configuration(), visitor.renderProps()),
                    length,
                    visitor.length()
                )
            })
    }

    companion object {
        fun create(): SimpleExtPlugin {
            return SimpleExtPlugin()
        }

        fun create(configure: SimpleExtConfigure): SimpleExtPlugin {
            val plugin = SimpleExtPlugin()
            configure.configure(plugin)
            return plugin
        }
    }
}
