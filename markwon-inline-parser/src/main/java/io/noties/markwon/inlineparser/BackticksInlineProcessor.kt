package io.noties.markwon.inlineparser

import org.commonmark.internal.util.Parsing
import org.commonmark.node.Code
import org.commonmark.node.Node
import java.util.regex.Pattern

/**
 * Parses inline code surrounded with `` ` `` chars `` `code` ``
 *
 * @since 4.2.0
 */
class BackticksInlineProcessor : InlineProcessor() {
    override fun specialCharacter(): Char {
        return '`'
    }

    override fun parse(): Node? {
        val ticks = match(TICKS_HERE)
        if (ticks == null) {
            return null
        }

        val afterOpenTicks = index
        var matched: String?
        while ((match(TICKS).also { matched = it }) != null) {
            if (matched == ticks) {
                val node = Code()
                var content = input.substring(afterOpenTicks, index - ticks.length)
                content = content.replace('\n', ' ')

                // spec: If the resulting string both begins and ends with a space character, but does not consist
                // entirely of space characters, a single space character is removed from the front and back.
                if (content.length >= 3 && content[0] == ' ' && content[content.length - 1] == ' ' && Parsing.hasNonSpace(
                        content
                    )
                ) {
                    content = content.substring(1, content.length - 1)
                }

                node.literal = content
                return node
            }
        }
        // If we got here, we didn't match a closing backtick sequence.
        index = afterOpenTicks
        return text(ticks)
    }

    companion object {
        private val TICKS: Pattern = Pattern.compile("`+")

        private val TICKS_HERE: Pattern = Pattern.compile("^`+")
    }
}
