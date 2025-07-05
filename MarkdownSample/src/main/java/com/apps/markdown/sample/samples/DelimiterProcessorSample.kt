package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import org.commonmark.node.Emphasis
import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun

@MarkwonSampleInfo(
    id = "20200630194017",
    title = "Custom delimiter processor",
    description = "Custom parsing delimiter processor with `?` character",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.PARSING]
)
class DelimiterProcessorSample : MarkwonTextViewSample() {
    override fun render() {
        val md = "" + "?hello? there!"

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureParser(builder: Parser.Builder) {
                builder.customDelimiterProcessor(QuestionDelimiterProcessor())
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}

internal class QuestionDelimiterProcessor : DelimiterProcessor {
    override fun getOpeningCharacter(): Char = '?'

    override fun getClosingCharacter(): Char = '?'
    override fun getMinLength(): Int = 1

    override fun getDelimiterUse(opener: DelimiterRun, closer: DelimiterRun): Int {
        if (opener.length() >= 1 && closer.length() >= 1) {
            return 1
        }
        return 0
    }

    override fun process(opener: Text, closer: Text?, delimiterUse: Int) {
        val node: Node = Emphasis()

        var tmp = opener.next
        while (tmp != null && tmp !== closer) {
            val next = tmp.next
            node.appendChild(tmp)
            tmp = next
        }

        opener.insertAfter(node)
    }
}