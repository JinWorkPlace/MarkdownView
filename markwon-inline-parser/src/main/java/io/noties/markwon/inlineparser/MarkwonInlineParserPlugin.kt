package io.noties.markwon.inlineparser

import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParser.FactoryBuilder
import org.commonmark.parser.Parser

/**
 * @since 4.3.0
 */
class MarkwonInlineParserPlugin internal constructor(
    private val factoryBuilder: FactoryBuilder
) : AbstractMarkwonPlugin() {
    interface BuilderConfigure<B : FactoryBuilder> {
        fun configureBuilder(factoryBuilder: B)
    }

    override fun configureParser(builder: Parser.Builder) {
        builder.inlineParserFactory(factoryBuilder.build())
    }

    fun factoryBuilder(): FactoryBuilder {
        return factoryBuilder
    }

    companion object {
        fun create(configure: BuilderConfigure<FactoryBuilder>): MarkwonInlineParserPlugin {
            val factoryBuilder = MarkwonInlineParser.factoryBuilder()
            configure.configureBuilder(factoryBuilder)
            return MarkwonInlineParserPlugin(factoryBuilder)
        }

        @JvmOverloads
        fun create(factoryBuilder: FactoryBuilder = MarkwonInlineParser.factoryBuilder()): MarkwonInlineParserPlugin {
            return MarkwonInlineParserPlugin(factoryBuilder)
        }

        fun <B : FactoryBuilder> create(
            factoryBuilder: B, configure: BuilderConfigure<B>
        ): MarkwonInlineParserPlugin {
            configure.configureBuilder(factoryBuilder)
            return MarkwonInlineParserPlugin(factoryBuilder)
        }
    }
}
