package com.apps.markdown.sample.samples.inlineparsing

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.inlineparser.CloseBracketInlineProcessor
import io.noties.markwon.inlineparser.MarkwonInlineParser
import io.noties.markwon.inlineparser.OpenBracketInlineProcessor
import org.commonmark.parser.InlineParserFactory

@MarkwonSampleInfo(
    id = "20200630170412",
    title = "Links only inline parsing",
    artifacts = [MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.PARSING, Tag.INLINE]
)
class InlineParsingLinksOnlySample : MarkwonTextViewSample() {
    override fun render() {
        // note that image is considered a link now
        val md = "**bold_bold-italic_** <u>html-u</u>, [link](#) ![alt](#image) `code`"

        // create an inline-parser-factory that will _ONLY_ parse links
        //  this would mean:
        //  * no emphasises (strong and regular aka bold and italics),
        //  * no images,
        //  * no code,
        //  * no HTML entities (&amp;)
        //  * no HTML tags
        // markdown blocks are still parsed
        val inlineParserFactory: InlineParserFactory =
            MarkwonInlineParser.factoryBuilderNoDefaults().referencesEnabled(true)
                .addInlineProcessor(OpenBracketInlineProcessor())
                .addInlineProcessor(CloseBracketInlineProcessor()).build()

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureParser(builder: org.commonmark.parser.Parser.Builder) {
                    builder.inlineParserFactory(inlineParserFactory)
                }
            }).build()

        markwon.setMarkdown(textView, md)
    }
}
