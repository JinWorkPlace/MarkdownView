package io.noties.markwon.inlineparser

import org.commonmark.internal.Bracket
import org.commonmark.node.Node

/**
 * Parses markdown images `![alt](#href)`
 *
 * @since 4.2.0
 */
class BangInlineProcessor : InlineProcessor() {
    override fun specialCharacter(): Char {
        return '!'
    }

    override fun parse(): Node? {
        val startIndex = index
        index++
        if (peek() == '[') {
            index++

            val node = text("![")

            // Add entry to stack for this opener
            addBracket(Bracket.image(node, startIndex + 1, lastBracket(), lastDelimiter()))

            return node
        } else {
            return null
        }
    }
}
