package io.noties.markwon.simple.ext

import io.noties.markwon.SpanFactory
import org.commonmark.parser.delimiter.DelimiterProcessor

// @since 4.0.0
internal class SimpleExtBuilder {
    private val extensions: MutableList<DelimiterProcessor> = ArrayList(2)

    private var isBuilt = false

    fun addExtension(length: Int, character: Char, spanFactory: SpanFactory) {
        checkState()

        extensions.add(SimpleExtDelimiterProcessor(character, character, length, spanFactory))
    }

    fun addExtension(
        length: Int,
        openingCharacter: Char,
        closingCharacter: Char,
        spanFactory: SpanFactory
    ) {
        checkState()

        extensions.add(
            SimpleExtDelimiterProcessor(
                openingCharacter,
                closingCharacter,
                length,
                spanFactory
            )
        )
    }

    fun build(): MutableList<DelimiterProcessor> {
        checkState()

        isBuilt = true

        return extensions
    }

    private fun checkState() {
        check(!isBuilt) { "SimpleExtBuilder is already built, " + "do not mutate SimpleExtPlugin after configuration is finished" }
    }
}
