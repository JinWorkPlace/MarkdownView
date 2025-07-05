package com.apps.markdown.sample.samples.inlineparsing

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.inlineparser.BackticksInlineProcessor
import io.noties.markwon.inlineparser.MarkwonInlineParser
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin

@MarkwonSampleInfo(
    id = "20200630170823",
    title = "Inline parsing no defaults",
    description = "Parsing only inline code and disable all the rest",
    artifacts = [MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.INLINE, Tag.PARSING]
)
class InlineParsingNoDefaultsSample : MarkwonTextViewSample() {
    override fun render() {
        // a plugin with NO defaults registered

        val md = "no [links](#) for **you** `code`!"

        val markwon: Markwon = Markwon.builder(context)
            .usePlugin(MarkwonInlineParserPlugin.create(MarkwonInlineParser.factoryBuilderNoDefaults()))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configure(registry: io.noties.markwon.MarkwonPlugin.Registry) {
                    registry.require(
                        MarkwonInlineParserPlugin::class.java,
                        io.noties.markwon.MarkwonPlugin.Action { plugin: MarkwonInlineParserPlugin ->
                            plugin.factoryBuilder().addInlineProcessor(BackticksInlineProcessor())
                        })
                }
            }).build()

        markwon.setMarkdown(textView, md)
    }
}
