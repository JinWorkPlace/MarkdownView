package com.apps.markdown.sample.samples.parser

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import org.commonmark.node.Heading
import org.commonmark.parser.Parser
import org.commonmark.parser.block.BlockParserFactory
import org.commonmark.parser.block.BlockStart
import org.commonmark.parser.block.MatchedBlockParser
import org.commonmark.parser.block.ParserState

@MarkwonSampleInfo(
    id = "20201111221207",
    title = "Custom heading parser",
    description = "Custom heading block parser. Actual parser is not implemented",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.PARSING, Tag.HEADING]
)
class CustomHeadingParserSample : MarkwonTextViewSample() {
    override fun render() {
        val md = "#Head"
        val markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureParser(builder: Parser.Builder) {
                val enabled =
                    CorePlugin.enabledBlockTypes().filter { it != Heading::class.java }.toSet()
                builder.enabledBlockTypes(enabled)
                builder.customBlockParserFactory(MyHeadingBlockParserFactory)
            }
        }).build()
        markwon.setMarkdown(textView, md)
    }

    object MyHeadingBlockParserFactory : BlockParserFactory {
        override fun tryStart(
            state: ParserState, matchedBlockParser: MatchedBlockParser
        ): BlockStart? {
            return null
        }
    }
}