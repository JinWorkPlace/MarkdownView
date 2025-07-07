package com.apps.markdown.sample.samples.inlineparsing

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.inlineparser.HtmlInlineProcessor
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import org.commonmark.node.Block
import org.commonmark.node.HtmlBlock
import org.commonmark.parser.Parser

@MarkwonSampleInfo(
    id = "20200630171239",
    title = "Inline parsing exclude HTML",
    artifacts = [MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.PARSING, Tag.INLINE, Tag.BLOCK]
)
class InlineParsingNoHtmlSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "# Html <b>disabled</b>\n\n" + "<em>emphasis <strong>strong</strong>\n\n" + "<p>paragraph <img src='hey.jpg' /></p>\n\n" + "<test></test>\n\n" + "<test>"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(MarkwonInlineParserPlugin.create())
                .usePlugin(object : AbstractMarkwonPlugin() {
                    override fun configure(registry: io.noties.markwon.MarkwonPlugin.Registry) {
                        registry.require(
                            MarkwonInlineParserPlugin::class.java,
                            io.noties.markwon.MarkwonPlugin.Action { plugin: MarkwonInlineParserPlugin ->
                                plugin.factoryBuilder()
                                    .excludeInlineProcessor(HtmlInlineProcessor::class.java)
                            })
                    }

                    override fun configureParser(builder: Parser.Builder) {
                        val blocks: MutableSet<Class<out Block>> = CorePlugin.enabledBlockTypes()
                        blocks.remove(HtmlBlock::class.java)

                        builder.enabledBlockTypes(blocks)
                    }
                }).build()

        markwon.setMarkdown(textView, md)
    }
}
