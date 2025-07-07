package com.apps.markdown.sample.samples.inlineparsing

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.inlineparser.OpenBracketInlineProcessor

@MarkwonSampleInfo(
    id = "20200630170723",
    title = "Inline parsing with defaults",
    description = "Parsing with all defaults except links",
    artifacts = [MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.INLINE, Tag.PARSING]
)
class InlineParsingWithDefaultsSample : MarkwonTextViewSample() {
    override fun render() {
        // a plugin with defaults registered

        val md = "no [links](#) for **you** `code`!"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(MarkwonInlineParserPlugin.create()) // the same as:
                .usePlugin(object : AbstractMarkwonPlugin() {
                    override fun configure(registry: io.noties.markwon.MarkwonPlugin.Registry) {
                        registry.require(
                            MarkwonInlineParserPlugin::class.java,
                            io.noties.markwon.MarkwonPlugin.Action { plugin: MarkwonInlineParserPlugin ->
                                plugin.factoryBuilder()
                                    .excludeInlineProcessor(OpenBracketInlineProcessor::class.java)
                            })
                    }
                }).build()

        markwon.setMarkdown(textView, md)
    }
}
