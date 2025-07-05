package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.Markwon.Companion.builder
import io.noties.markwon.inlineparser.MarkwonInlineParser
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin


@MarkwonSampleInfo(
    id = "20200629170857",
    title = "Inline parsing without defaults",
    description = "Configure inline parser plugin to **not** have any **inline** parsing",
    artifacts = [MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.PARSING]
)
class InlinePluginNoDefaultsSample : MarkwonTextViewSample() {
    override fun render() {
        val md = "" + "# Heading\n" + "`code` inlined and **bold** here"

        val markwon: Markwon =
            builder(context).usePlugin(MarkwonInlineParserPlugin.create(MarkwonInlineParser.factoryBuilderNoDefaults()))
                .build()

        markwon.setMarkdown(textView, md)
    }
}
