package io.noties.markwon.ext.latex

import org.commonmark.internal.util.Parsing
import org.commonmark.node.Block
import org.commonmark.parser.SourceLine
import org.commonmark.parser.block.AbstractBlockParser
import org.commonmark.parser.block.AbstractBlockParserFactory
import org.commonmark.parser.block.BlockContinue
import org.commonmark.parser.block.BlockStart
import org.commonmark.parser.block.MatchedBlockParser
import org.commonmark.parser.block.ParserState

/**
 * @since 4.3.0 (although there was a class with the same name,
 * which is renamed now to [JLatexMathBlockParserLegacy])
 */
internal class JLatexMathBlockParser(private val signs: Int) : AbstractBlockParser() {
    private val block = JLatexMathBlock()

    private val builder = StringBuilder()

    override fun getBlock(): Block {
        return block
    }

    override fun tryContinue(parserState: ParserState): BlockContinue {
        val nextNonSpaceIndex = parserState.nextNonSpaceIndex
        val line: CharSequence = parserState.line.content
        val length = line.length

        // check for closing
        if (parserState.indent < Parsing.CODE_BLOCK_INDENT) {
            if (consume(DOLLAR, line, nextNonSpaceIndex, length) == signs) {
                // okay, we have our number of signs
                // let's consume spaces until the end
                if (Parsing.skip(
                        SPACE, line, nextNonSpaceIndex + signs, length
                    ) == length
                ) {
                    return BlockContinue.finished()
                }
            }
        }

        return BlockContinue.atIndex(parserState.index)
    }

    override fun addLine(line: SourceLine) {
        builder.append(line)
        builder.append('\n')
    }

    override fun closeBlock() {
        block.latex(builder.toString())
    }

    class Factory : AbstractBlockParserFactory() {
        override fun tryStart(
            state: ParserState, matchedBlockParser: MatchedBlockParser?
        ): BlockStart? {
            // let's define the spec:
            //  * 0-3 spaces before are allowed (Parsing.CODE_BLOCK_INDENT = 4)
            //  * 2+ subsequent `$` signs
            //  * any optional amount of spaces
            //  * new line
            //  * block is closed when the same amount of opening signs is met

            val indent = state.indent

            // check if it's an indented code block
            if (indent >= Parsing.CODE_BLOCK_INDENT) {
                return BlockStart.none()
            }

            val nextNonSpaceIndex = state.nextNonSpaceIndex
            val line: CharSequence = state.line.content
            val length = line.length

            val signs: Int = consume(DOLLAR, line, nextNonSpaceIndex, length)

            // 2 is minimum
            if (signs < 2) {
                return BlockStart.none()
            }

            // consume spaces until the end of the line, if any other content is found -> NONE
            if (Parsing.skip(SPACE, line, nextNonSpaceIndex + signs, length) != length) {
                return BlockStart.none()
            }

            return BlockStart.of(JLatexMathBlockParser(signs)).atIndex(length + 1)
        }
    }

    companion object {
        private const val DOLLAR = '$'
        private const val SPACE = ' '

        private fun consume(c: Char, line: CharSequence, start: Int, end: Int): Int {
            for (i in start..<end) {
                if (c != line[i]) {
                    return i - start
                }
            }
            // all consumed
            return end - start
        }
    }
}
