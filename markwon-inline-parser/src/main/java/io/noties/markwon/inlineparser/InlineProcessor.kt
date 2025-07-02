package io.noties.markwon.inlineparser

import org.commonmark.internal.Bracket
import org.commonmark.internal.Delimiter
import org.commonmark.node.Node
import org.commonmark.node.Text
import java.util.regex.Pattern

/**
 * @see AutolinkInlineProcessor
 *
 * @see BackslashInlineProcessor
 *
 * @see BackticksInlineProcessor
 *
 * @see BangInlineProcessor
 *
 * @see CloseBracketInlineProcessor
 *
 * @see EntityInlineProcessor
 *
 * @see HtmlInlineProcessor
 *
 * @see NewLineInlineProcessor
 *
 * @see OpenBracketInlineProcessor
 *
 * @see MarkwonInlineParser.FactoryBuilder.addInlineProcessor
 * @see MarkwonInlineParser.FactoryBuilder.excludeInlineProcessor
 * @since 4.2.0
 */
abstract class InlineProcessor {
    /**
     * Special character that triggers parsing attempt
     */
    abstract fun specialCharacter(): Char

    /**
     * @return boolean indicating if parsing succeeded
     */
    protected abstract fun parse(): Node?


    protected var context: MarkwonInlineParserContext? = null
    protected var block: Node? = null
    protected var input: String? = null
    protected var index: Int = 0

    fun parse(context: MarkwonInlineParserContext): Node? {
        this.context = context
        this.block = context.block()
        this.input = context.input()
        this.index = context.index()

        val result = parse()

        // synchronize index
        context.setIndex(index)

        return result
    }

    protected fun lastBracket(): Bracket? {
        return context!!.lastBracket()
    }

    protected fun lastDelimiter(): Delimiter? {
        return context!!.lastDelimiter()
    }

    protected fun addBracket(bracket: Bracket?) {
        context!!.addBracket(bracket)
    }

    protected fun removeLastBracket() {
        context!!.removeLastBracket()
    }

    protected fun spnl() {
        context!!.setIndex(index)
        context!!.spnl()
        index = context!!.index()
    }

    protected fun match(re: Pattern): String? {
        // before trying to match, we must notify context about our index (which we store additionally here)
        context!!.setIndex(index)

        val result = context!!.match(re)

        // after match we must reflect index change here
        this.index = context!!.index()

        return result
    }

    protected fun parseLinkDestination(): String? {
        context!!.setIndex(index)
        val result = context!!.parseLinkDestination()
        this.index = context!!.index()
        return result
    }

    protected fun parseLinkTitle(): String? {
        context!!.setIndex(index)
        val result = context!!.parseLinkTitle()
        this.index = context!!.index()
        return result
    }

    protected fun parseLinkLabel(): Int {
        context!!.setIndex(index)
        val result = context!!.parseLinkLabel()
        this.index = context!!.index()
        return result
    }

    protected fun processDelimiters(stackBottom: Delimiter?) {
        context!!.setIndex(index)
        context!!.processDelimiters(stackBottom)
        this.index = context!!.index()
    }

    protected fun text(text: String): Text {
        return context!!.text(text)
    }

    protected fun text(text: String, start: Int, end: Int): Text {
        return context!!.text(text, start, end)
    }

    protected fun peek(): Char {
        context!!.setIndex(index)
        return context!!.peek()
    }
}
