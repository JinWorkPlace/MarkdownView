package io.noties.markwon.simple.ext

import io.noties.markwon.SpanFactory
import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun

// @since 4.0.0
internal data class SimpleExtDelimiterProcessor(
    private val open: Char,
    private val close: Char,
    private val length: Int,
    private val spanFactory: SpanFactory
) : DelimiterProcessor {
    override fun getOpeningCharacter(): Char {
        return open
    }

    override fun getClosingCharacter(): Char {
        return close
    }

    override fun getMinLength(): Int {
        return length
    }

    override fun getDelimiterUse(opener: DelimiterRun, closer: DelimiterRun): Int {
        if (opener.length() >= length && closer.length() >= length) {
            return length
        }
        return 0
    }

    override fun process(opener: Text, closer: Text?, delimiterUse: Int) {
        val node: Node = SimpleExtNode(spanFactory)

        var tmp = opener.next
        var next: Node?

        while (tmp != null && tmp != closer) {
            next = tmp.next
            node.appendChild(tmp)
            tmp = next
        }

        opener.insertAfter(node)
    }
}
