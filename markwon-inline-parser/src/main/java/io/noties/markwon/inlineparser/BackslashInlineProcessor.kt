package io.noties.markwon.inlineparser

import org.commonmark.node.HardLineBreak
import org.commonmark.node.Node
import java.util.regex.Pattern

/**
 * @since 4.2.0
 */
class BackslashInlineProcessor : InlineProcessor() {
    override fun specialCharacter(): Char {
        return '\\'
    }

    override fun parse(): Node {
        index++
        val node: Node
        if (peek() == '\n') {
            node = HardLineBreak()
            index++
        } else if (index < input.length && ESCAPABLE.matcher(input.substring(index, index + 1))
                .matches()
        ) {
            node = text(input, index, index + 1)
            index++
        } else {
            node = text("\\")
        }
        return node
    }

    companion object {
        private val ESCAPABLE: Pattern = MarkwonInlineParser.ESCAPABLE
    }
}
