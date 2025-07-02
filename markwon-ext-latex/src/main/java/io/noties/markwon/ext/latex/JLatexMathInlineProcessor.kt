package io.noties.markwon.ext.latex

import io.noties.markwon.inlineparser.InlineProcessor
import org.commonmark.node.Node
import java.util.regex.Pattern

/**
 * @since 4.3.0
 */
internal class JLatexMathInlineProcessor : InlineProcessor() {
    override fun specialCharacter(): Char {
        return '$'
    }

    override fun parse(): Node? {
        val latex = match(RE)
        if (latex == null) {
            return null
        }

        val node = JLatexMathNode()
        node.latex(latex.substring(2, latex.length - 2))
        return node
    }

    companion object {
        private val RE: Pattern = Pattern.compile("(\\\${2})([\\s\\S]+?)\\1")
    }
}
