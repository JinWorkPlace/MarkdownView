package io.noties.markwon.inlineparser

import org.commonmark.internal.util.Parsing
import org.commonmark.node.HtmlInline
import org.commonmark.node.Node
import java.util.regex.Pattern

/**
 * Parses inline HTML tags
 *
 * @since 4.2.0
 */
class HtmlInlineProcessor : InlineProcessor() {
    override fun specialCharacter(): Char {
        return '<'
    }

    override fun parse(): Node? {
        val m = match(HTML_TAG)
        if (m != null) {
            val node = HtmlInline()
            node.literal = m
            return node
        } else {
            return null
        }
    }

    companion object {
        private const val HTMLCOMMENT = "<!---->|<!--(?:-?[^>-])(?:-?[^-])*-->"

        private const val PROCESSINGINSTRUCTION = "[<][?].*?[?][>]"

        private const val DECLARATION = "<![A-Z]+\\s+[^>]*>"

        private const val CDATA = "<!\\[CDATA\\[[\\s\\S]*?\\]\\]>"

        private const val HTMLTAG =
            ("(?:" + Parsing.OPENTAG + "|" + Parsing.CLOSETAG + "|" + HTMLCOMMENT + "|" + PROCESSINGINSTRUCTION + "|" + DECLARATION + "|" + CDATA + ")")

        private val HTML_TAG: Pattern = Pattern.compile("^$HTMLTAG", Pattern.CASE_INSENSITIVE)
    }
}
