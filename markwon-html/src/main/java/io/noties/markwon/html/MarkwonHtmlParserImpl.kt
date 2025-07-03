package io.noties.markwon.html

import androidx.annotation.VisibleForTesting
import io.noties.markwon.html.AppendableUtils.appendQuietly
import io.noties.markwon.html.HtmlTagImpl.BlockImpl.Companion.root
import io.noties.markwon.html.HtmlTagImpl.InlineImpl
import io.noties.markwon.html.jsoup.parser.CharacterReader
import io.noties.markwon.html.jsoup.parser.ParseErrorList.Companion.noTracking
import io.noties.markwon.html.jsoup.parser.Token
import io.noties.markwon.html.jsoup.parser.Tokeniser
import java.util.Collections

/**
 * @since 2.0.0
 */
open class MarkwonHtmlParserImpl internal constructor(
    private val emptyTagReplacement: HtmlEmptyTagReplacement,
    private val trimmingAppender: TrimmingAppender
) : MarkwonHtmlParser() {
    private val inlineTags: MutableList<InlineImpl> = ArrayList(0)

    private var currentBlock: HtmlTagImpl.BlockImpl? = root()

    private var isInsidePreTag = false

    // the thing is: we ensure a new line BEFORE block tag
    // but not after, so another tag will be placed on the same line (which is wrong)
    private var previousIsBlock = false


    override fun <T> processFragment(
        output: T, htmlFragment: String
    ) where T : Appendable, T : CharSequence {
        // we might want to reuse tokeniser (at least when the same output is involved)
        // as CharacterReader does a bit of initialization (cache etc) as it's
        // primary usage is parsing a document in one run (not parsing _fragments_)

        val tokeniser = Tokeniser(CharacterReader(htmlFragment), noTracking())

        while (true) {
            val token = tokeniser.read()
            val tokenType = token!!.type

            if (Token.TokenType.EOF == tokenType) {
                break
            }

            when (tokenType) {
                Token.TokenType.StartTag -> {
                    val startTag = token as Token.StartTag

                    if (isInlineTag(startTag.normalName!!)) {
                        processInlineTagStart(output, startTag)
                    } else {
                        processBlockTagStart(output, startTag)
                    }
                }

                Token.TokenType.EndTag -> {
                    val endTag = token as Token.EndTag

                    if (isInlineTag(endTag.normalName!!)) {
                        processInlineTagEnd(output, endTag)
                    } else {
                        processBlockTagEnd(output, endTag)
                    }
                }

                Token.TokenType.Character -> {
                    processCharacter(output, (token as Token.Character))
                }

                Token.TokenType.Doctype -> {
                    //todo
                }

                Token.TokenType.Comment -> {
                    //todo
                }

                Token.TokenType.EOF -> {
                    //todo
                }
            }

            // do not forget to reset processed token (even if it's not processed)
            token.reset()
        }
    }

    override fun flushInlineTags(documentLength: Int, action: FlushAction<HtmlTag.Inline>) {
        if (!inlineTags.isEmpty()) {
            if (documentLength > HtmlTag.NO_END) {
                for (inline in inlineTags) {
                    inline.closeAt(documentLength)
                }
            }

            action.apply(Collections.unmodifiableList(inlineTags))
            inlineTags.clear()
        } else {
            action.apply(mutableListOf())
        }
    }

    override fun flushBlockTags(documentLength: Int, action: FlushAction<HtmlTag.Block>) {
        var block = currentBlock
        while (block!!.parent != null) {
            block = block.parent
        }

        if (documentLength > HtmlTag.NO_END) {
            block.closeAt(documentLength)
        }

        val children: MutableList<HtmlTag.Block> = block.children as MutableList<HtmlTag.Block>
        if (!children.isEmpty()) {
            action.apply(children)
        } else {
            action.apply(mutableListOf())
        }

        currentBlock = root()
    }

    override fun reset() {
        inlineTags.clear()
        currentBlock = root()
    }


    protected fun <T> processInlineTagStart(
        output: T, startTag: Token.StartTag
    ) where T : Appendable?, T : CharSequence? {
        val name = startTag.normalName

        val inline = InlineImpl(name!!, output!!.length, extractAttributes(startTag))

        ensureNewLineIfPreviousWasBlock<T?>(output)

        if (isVoidTag(name) || startTag.isSelfClosing) {
            val replacement = emptyTagReplacement.replace(inline)
            if (replacement != null && !replacement.isEmpty()) {
                appendQuietly(output, replacement)
            }

            // the thing is: we will keep this inline tag in the list,
            // but in case of void-tag that has no replacement, there will be no
            // possibility to set a span (requires at least one char)
            inline.closeAt(output.length)
        }

        inlineTags.add(inline)
    }

    protected fun <T> processInlineTagEnd(
        output: T, endTag: Token.EndTag
    ) where T : Appendable?, T : CharSequence? {
        // try to find it, if none found -> ignore

        val openInline = findOpenInlineTag(endTag.normalName!!)
        if (openInline != null) {
            // okay, if this tag is empty -> call replacement

            if (isEmpty(output, openInline)) {
                appendEmptyTagReplacement(output, openInline)
            }

            // close open inline tag
            openInline.closeAt(output!!.length)
        }
    }


    protected fun <T> processBlockTagStart(
        output: T, startTag: Token.StartTag
    ) where T : Appendable?, T : CharSequence? {
        val name = startTag.normalName

        // block tags (all that are NOT inline -> blocks
        // there is only one strong rule -> paragraph cannot contain anything
        // except inline tags
        if (TAG_PARAGRAPH == currentBlock!!.name) {
            // it must be closed here not matter what we are as here we _assume_
            // that it's a block tag
            currentBlock!!.closeAt(output!!.length)
            appendQuietly(output, '\n')
            currentBlock = currentBlock!!.parent
        } else if (TAG_LIST_ITEM == name && TAG_LIST_ITEM == currentBlock!!.name) {
            // close previous list item if in the same parent
            currentBlock!!.closeAt(output!!.length)
            currentBlock = currentBlock!!.parent
        }

        if (isBlockTag(name!!)) {
            isInsidePreTag = "pre" == name
            ensureNewLine(output)
        } else {
            ensureNewLineIfPreviousWasBlock(output)
        }

        val start = output!!.length

        val block = HtmlTagImpl.BlockImpl.Companion.create(
            name, start, extractAttributes(startTag), currentBlock
        )

        val isVoid = isVoidTag(name) || startTag.isSelfClosing
        if (isVoid) {
            val replacement = emptyTagReplacement.replace(block)
            if (replacement != null && !replacement.isEmpty()) {
                appendQuietly(output, replacement)
            }
            block.closeAt(output.length)
        }

        appendBlockChild(block.parent!!, block)

        // if not void start filling-in children
        if (!isVoid) {
            this.currentBlock = block
        }
    }

    protected fun <T> processBlockTagEnd(
        output: T, endTag: Token.EndTag
    ) where T : Appendable?, T : CharSequence? {
        val name = endTag.normalName

        val block = findOpenBlockTag(endTag.normalName!!)
        if (block != null) {
            if ("pre" == name) {
                isInsidePreTag = false
            }

            // okay, if this tag is empty -> call replacement
            if (isEmpty(output, block)) {
                appendEmptyTagReplacement(output, block)
            }

            block.closeAt(output!!.length)

            // if it's empty -> we do no care about if it's block or not
            if (!block.isEmpty) {
                previousIsBlock = isBlockTag(block.name)
            }

            if (TAG_PARAGRAPH == name) {
                AppendableUtils.appendQuietly(output, '\n')
            }

            this.currentBlock = block.parent
        }
    }

    protected fun <T> processCharacter(
        output: T, character: Token.Character
    ) where T : Appendable?, T : CharSequence? {
        // there are tags: BUTTON, INPUT, SELECT, SCRIPT, TEXTAREA, STYLE
        // that might have character data that we do not want to display

        if (isInsidePreTag) {
            appendQuietly(output!!, character.getData())
        } else {
            ensureNewLineIfPreviousWasBlock(output)
            trimmingAppender.append<T?>(output!!, character.getData())
        }
    }

    protected fun appendBlockChild(parent: HtmlTagImpl.BlockImpl, child: HtmlTagImpl.BlockImpl) {
        var children: MutableList<HtmlTagImpl.BlockImpl>? = parent.children
        if (children == null) {
            children = ArrayList(2)
            parent.children = children
        }
        children.add(child)
    }

    protected fun findOpenInlineTag(name: String): InlineImpl? {
        var inline: InlineImpl

        for (i in inlineTags.size - 1 downTo -1 + 1) {
            inline = inlineTags[i]
            if (name == inline.name && inline.end < 0) {
                return inline
            }
        }

        return null
    }

    protected fun findOpenBlockTag(name: String): HtmlTagImpl.BlockImpl? {
        var blockTag = currentBlock

        while (blockTag != null && (name != blockTag.name) && !blockTag.isClosed) {
            blockTag = blockTag.parent
        }

        return blockTag
    }

    protected fun <T> ensureNewLineIfPreviousWasBlock(output: T) where T : Appendable?, T : CharSequence? {
        if (previousIsBlock) {
            ensureNewLine(output)
            previousIsBlock = false
        }
    }

    protected fun <T> appendEmptyTagReplacement(
        output: T, tag: HtmlTagImpl
    ) where T : Appendable?, T : CharSequence? {
        val replacement = emptyTagReplacement.replace(tag)
        if (replacement != null) {
            appendQuietly(output!!, replacement)
        }
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun create(inlineTagReplacement: HtmlEmptyTagReplacement = HtmlEmptyTagReplacement.create()): MarkwonHtmlParserImpl {
            return MarkwonHtmlParserImpl(inlineTagReplacement, TrimmingAppender.create())
        }

        // https://developer.mozilla.org/en-US/docs/Web/HTML/Inline_elements
        @JvmField
        @VisibleForTesting
        val INLINE_TAGS: MutableSet<String?> = mutableSetOf<String?>(
            "a",
            "abbr",
            "acronym",
            "b",
            "bdo",
            "big",
            "br",
            "button",
            "cite",
            "code",
            "dfn",
            "em",
            "i",
            "img",
            "input",
            "kbd",
            "label",
            "map",
            "object",
            "q",
            "samp",
            "script",
            "select",
            "small",
            "span",
            "strong",
            "sub",
            "sup",
            "textarea",
            "time",
            "tt",
            "var"
        )

        private val VOID_TAGS: MutableSet<String?>

        // these are the tags that are considered _block_ ones
        // this parser will ensure that these blocks are started on a new line
        // other tags that are NOT inline are considered as block tags, but won't have new line
        // inserted before them
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Block-level_elements
        private val BLOCK_TAGS: MutableSet<String?>

        private const val TAG_PARAGRAPH = "p"
        private const val TAG_LIST_ITEM = "li"

        init {
            VOID_TAGS = mutableSetOf<String?>(
                "area",
                "base",
                "br",
                "col",
                "embed",
                "hr",
                "img",
                "input",
                "keygen",
                "link",
                "meta",
                "param",
                "source",
                "track",
                "wbr"
            )
            BLOCK_TAGS = mutableSetOf<String?>(
                "address",
                "article",
                "aside",
                "blockquote",
                "canvas",
                "dd",
                "div",
                "dl",
                "dt",
                "fieldset",
                "figcaption",
                "figure",
                "footer",
                "form",
                "h1",
                "h2",
                "h3",
                "h4",
                "h5",
                "h6",
                "header",
                "hgroup",
                "hr",
                "li",
                "main",
                "nav",
                "noscript",
                "ol",
                "output",
                "p",
                "pre",
                "section",
                "table",
                "tfoot",
                "ul",
                "video"
            )
        }

        // name here must lower case
        protected fun isInlineTag(name: String): Boolean {
            return INLINE_TAGS.contains(name)
        }

        protected fun isVoidTag(name: String): Boolean {
            return VOID_TAGS.contains(name)
        }

        protected fun isBlockTag(name: String): Boolean {
            return BLOCK_TAGS.contains(name)
        }

        protected fun <T> ensureNewLine(output: T) where T : Appendable?, T : CharSequence? {
            val length = output!!.length
            if (length > 0 && '\n' != output[length - 1]) {
                appendQuietly(output, '\n')
            }
        }

        protected fun extractAttributes(startTag: Token.StartTag): MutableMap<String, String> {
            var map: MutableMap<String, String>

            val attributes = startTag.attributes
            val size = attributes!!.size()

            if (size > 0) {
                map = HashMap(size)
                for (attribute in attributes) {
                    map.put(attribute.key!!.lowercase(), attribute.value ?: "")
                }
                map = Collections.unmodifiableMap(map)
            } else {
                map = mutableMapOf()
            }

            return map
        }

        protected fun <T> isEmpty(
            output: T, tag: HtmlTagImpl
        ): Boolean where T : Appendable?, T : CharSequence? {
            return tag.start == output!!.length
        }
    }
}
