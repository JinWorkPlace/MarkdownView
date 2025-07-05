package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.inlineparser.MarkwonInlineParser
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import org.commonmark.node.Block
import org.commonmark.parser.Parser

@MarkwonSampleInfo(
    id = "20200629171212",
    title = "No parsing",
    description = "All commonmark parsing is disabled (both inlines and blocks)",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.PARSING, Tag.RENDERING]
)
class NoParsingSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Heading\n" + "[link](#) was _here_ and `then` and it was:\n" + "> a quote\n" + "```java\n" + "final int someJavaCode = 0;\n" + "```\n"

        val markwon: Markwon = Markwon.builder(context) // disable inline parsing
            .usePlugin(MarkwonInlineParserPlugin.create(MarkwonInlineParser.factoryBuilderNoDefaults()))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureParser(builder: Parser.Builder) {
                    builder.enabledBlockTypes(kotlin.collections.mutableSetOf<Class<out Block>>())
                }
            }).build()

        markwon.setMarkdown(textView, md)
    }
}
