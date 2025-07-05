package com.apps.markdown.sample.samples.editor

import android.text.method.LinkMovementMethod
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.samples.editor.shared.BlockQuoteEditHandler
import com.apps.markdown.sample.samples.editor.shared.CodeEditHandler
import com.apps.markdown.sample.samples.editor.shared.LinkEditHandler
import com.apps.markdown.sample.samples.editor.shared.MarkwonEditTextSample
import com.apps.markdown.sample.samples.editor.shared.StrikethroughEditHandler
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import io.noties.markwon.editor.handler.EmphasisEditHandler
import io.noties.markwon.editor.handler.StrongEmphasisEditHandler
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.inlineparser.BangInlineProcessor
import io.noties.markwon.inlineparser.EntityInlineProcessor
import io.noties.markwon.inlineparser.HtmlInlineProcessor
import io.noties.markwon.inlineparser.MarkwonInlineParser
import io.noties.markwon.linkify.LinkifyPlugin
import org.commonmark.parser.InlineParserFactory

@MarkwonSampleInfo(
    id = "20200629165920",
    title = "Multiple edit spans",
    description = "Additional multiple edit spans for editor",
    artifacts = [MarkwonArtifact.EDITOR, MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.EDITOR]
)
class EditorMultipleEditSpansSample : MarkwonEditTextSample() {
    override fun render() {
        // for links to be clickable

        editText.movementMethod = LinkMovementMethod.getInstance()

        val inlineParserFactory: InlineParserFactory =
            MarkwonInlineParser.factoryBuilder() // no inline images will be parsed
                .excludeInlineProcessor(BangInlineProcessor::class.java) // no html tags will be parsed
                .excludeInlineProcessor(HtmlInlineProcessor::class.java) // no entities will be parsed (aka `&amp;` etc)
                .excludeInlineProcessor(EntityInlineProcessor::class.java).build()

        val markwon: Markwon = Markwon.builder(context).usePlugin(StrikethroughPlugin.create())
            .usePlugin(LinkifyPlugin.create()).usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureParser(builder: org.commonmark.parser.Parser.Builder) {
                    builder.inlineParserFactory(inlineParserFactory)
                }
            }).usePlugin(SoftBreakAddsNewLinePlugin.create()).build()

        val onClick: LinkEditHandler.OnClick = LinkEditHandler.OnClick { widget, link ->
            markwon.configuration().linkResolver().resolve(widget, link)
        }

        val editor: MarkwonEditor =
            MarkwonEditor.builder(markwon).useEditHandler(EmphasisEditHandler())
                .useEditHandler(StrongEmphasisEditHandler())
                .useEditHandler(StrikethroughEditHandler()).useEditHandler(CodeEditHandler())
                .useEditHandler(BlockQuoteEditHandler()).useEditHandler(LinkEditHandler(onClick))
                .build()

        editText.addTextChangedListener(
            MarkwonEditorTextWatcher.withPreRender(
                editor, java.util.concurrent.Executors.newSingleThreadExecutor(), editText
            )
        )
    }
}
