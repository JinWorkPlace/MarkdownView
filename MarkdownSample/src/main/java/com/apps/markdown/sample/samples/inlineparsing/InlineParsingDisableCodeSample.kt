package com.apps.markdown.sample.samples.inlineparsing

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.inlineparser.BackticksInlineProcessor
import io.noties.markwon.inlineparser.MarkwonInlineParser
import org.commonmark.node.Block
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.parser.InlineParserFactory
import org.commonmark.parser.Parser

@MarkwonSampleInfo(
    id = "20200630170607",
    title = "Disable code inline parsing",
    artifacts = [MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.INLINE, Tag.PARSING]
)
class InlineParsingDisableCodeSample : MarkwonTextViewSample() {
    override fun render() {
        // parses all as usual, but ignores code (inline and block)

        val md =
            "# Head!\n\n" + "* one\n" + "+ two\n\n" + "and **bold** to `you`!\n\n" + "> a quote _em_\n\n" + "```java\n" + "final int i = 0;\n" + "```\n\n" + "**Good day!**"

        val inlineParserFactory: InlineParserFactory = MarkwonInlineParser.factoryBuilder()
            .excludeInlineProcessor(BackticksInlineProcessor::class.java).build()

        val enabledBlocks: HashSet<Class<out Block>> = object : HashSet<Class<out Block>>() {
            init {
                addAll(CorePlugin.enabledBlockTypes())

                remove(FencedCodeBlock::class.java)
                remove(IndentedCodeBlock::class.java)
            }
        }

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureParser(builder: Parser.Builder) {
                builder.inlineParserFactory(inlineParserFactory).enabledBlockTypes(enabledBlocks)
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}
