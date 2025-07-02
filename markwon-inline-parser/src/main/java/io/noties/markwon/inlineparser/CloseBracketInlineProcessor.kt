package io.noties.markwon.inlineparser

import io.noties.markwon.inlineparser.InlineParserUtils.mergeChildTextNodes
import org.commonmark.internal.Bracket
import org.commonmark.internal.util.Escaping
import org.commonmark.node.Image
import org.commonmark.node.Link
import org.commonmark.node.Node
import java.util.regex.Pattern

/**
 * Parses markdown link or image, relies on [OpenBracketInlineProcessor]
 * to handle start of these elements
 *
 * @since 4.2.0
 */
class CloseBracketInlineProcessor : InlineProcessor() {
    override fun specialCharacter(): Char {
        return ']'
    }

    override fun parse(): Node? {
        index++
        val startIndex = index

        // Get previous `[` or `![`
        val opener = lastBracket()
        if (opener == null) {
            // No matching opener, just return a literal.
            return text("]")
        }

        if (!opener.allowed) {
            // Matching opener but it's not allowed, just return a literal.
            removeLastBracket()
            return text("]")
        }

        // Check to see if we have a link/image
        var dest: String? = null
        var title: String? = null
        var isLinkOrImage = false

        // Maybe a inline link like `[foo](/uri "title")`
        if (peek() == '(') {
            index++
            spnl()
            if ((parseLinkDestination().also { dest = it }) != null) {
                spnl()
                // title needs a whitespace before
                if (WHITESPACE.matcher(input.substring(index - 1, index)).matches()) {
                    title = parseLinkTitle()
                    spnl()
                }
                if (peek() == ')') {
                    index++
                    isLinkOrImage = true
                } else {
                    index = startIndex
                }
            }
        }

        // Maybe a reference link like `[foo][bar]`, `[foo][]` or `[foo]`
        if (!isLinkOrImage) {
            // See if there's a link label like `[bar]` or `[]`

            val beforeLabel = index
            parseLinkLabel()
            val ref = getString(beforeLabel, opener, startIndex)

            if (ref != null) {
                val label = Escaping.normalizeReference(ref)
                val definition = context?.getLinkReferenceDefinition(label)
                if (definition != null) {
                    dest = definition.destination
                    title = definition.title
                    isLinkOrImage = true
                }
            }
        }

        if (isLinkOrImage) {
            // If we got here, open is a potential opener
            val linkOrImage = if (opener.image) Image(dest, title) else Link(dest, title)

            var node = opener.node.next
            while (node != null) {
                val next = node.next
                linkOrImage.appendChild(node)
                node = next
            }

            // Process delimiters such as emphasis inside link/image
            processDelimiters(opener.previousDelimiter)
            mergeChildTextNodes(linkOrImage)
            // We don't need the corresponding text node anymore, we turned it into a link/image node
            opener.node.unlink()
            removeLastBracket()

            // Links within links are not allowed. We found this link, so there can be no other link around it.
            if (!opener.image) {
                var bracket = lastBracket()
                while (bracket != null) {
                    if (!bracket.image) {
                        // Disallow link opener. It will still get matched, but will not result in a link.
                        bracket.allowed = false
                    }
                    bracket = bracket.previous
                }
            }

            return linkOrImage
        } else { // no link or image
            index = startIndex
            removeLastBracket()

            return text("]")
        }
    }

    private fun getString(beforeLabel: Int, opener: Bracket, startIndex: Int): String? {
        val labelLength = index - beforeLabel
        var ref: String? = null
        if (labelLength > 2) {
            ref = input.substring(beforeLabel, beforeLabel + labelLength)
        } else if (!opener.bracketAfter) {
            // If the second label is empty `[foo][]` or missing `[foo]`, then the first label is the reference.
            // But it can only be a reference when there's no (unescaped) bracket in it.
            // If there is, we don't even need to try to look up the reference. This is an optimization.
            ref = input.substring(opener.index, startIndex)
        }
        return ref
    }

    companion object {
        private val WHITESPACE: Pattern = MarkwonInlineParser.WHITESPACE
    }
}
