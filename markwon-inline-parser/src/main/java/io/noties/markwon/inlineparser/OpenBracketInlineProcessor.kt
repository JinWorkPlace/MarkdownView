package io.noties.markwon.inlineparser

import org.commonmark.internal.Bracket
import org.commonmark.node.Node

/**
 * Parses markdown links `[link](#href)`
 *
 * @since 4.2.0
 */
class OpenBracketInlineProcessor : InlineProcessor() {
    override fun specialCharacter(): Char {
        return '['
    }

    override fun parse(): Node {
        val startIndex = index
        index++

        val node = text("[")

        // Add entry to stack for this opener
        addBracket(Bracket.link(node, startIndex, lastBracket(), lastDelimiter()))

        return node
    }
}
