package io.noties.markwon.ext.latex

import org.commonmark.node.Block
import org.commonmark.parser.block.AbstractBlockParser
import org.commonmark.parser.block.AbstractBlockParserFactory
import org.commonmark.parser.block.BlockContinue
import org.commonmark.parser.block.BlockStart
import org.commonmark.parser.block.MatchedBlockParser
import org.commonmark.parser.block.ParserState

/**
 * @since 4.3.0 (although it is just renamed parser from previous versions)
 */
internal class JLatexMathBlockParserLegacy : AbstractBlockParser() {
    private val block = JLatexMathBlock()

    private val builder = StringBuilder()

    private var isClosed = false

    override fun getBlock(): Block {
        return block
    }

    override fun tryContinue(parserState: ParserState): BlockContinue {
        if (isClosed) {
            return BlockContinue.finished()
        }

        return BlockContinue.atIndex(parserState.index)
    }

    override fun addLine(line: CharSequence) {
        if (builder.isNotEmpty()) {
            builder.append('\n')
        }

        builder.append(line)

        val length = builder.length
        if (length > 1) {
            isClosed = '$' == builder.get(length - 1) && '$' == builder.get(length - 2)
            if (isClosed) {
                builder.replace(length - 2, length, "")
            }
        }
    }

    override fun closeBlock() {
        block.latex(builder.toString())
    }

    class Factory : AbstractBlockParserFactory() {
        override fun tryStart(
            state: ParserState, matchedBlockParser: MatchedBlockParser?
        ): BlockStart? {
            val line: CharSequence? = state.line
            val length = line?.length ?: 0

            if (length > 1) {
                if ('$' == line!![0] && '$' == line[1]) {
                    return BlockStart.of(JLatexMathBlockParserLegacy()).atIndex(state.index + 2)
                }
            }

            return BlockStart.none()
        }
    }
}
