package io.noties.markwon.html.jsoup.parser

/**
 * A container for ParseErrors.
 *
 * @author Jonathan Hedley
 */
class ParseErrorList internal constructor(
    initialCapacity: Int, val maxSize: Int
) : ArrayList<ParseError>(initialCapacity) {
    fun canAddError(): Boolean {
        return size < maxSize
    }

    companion object {
        private const val INITIAL_CAPACITY = 16

        @JvmStatic
        fun noTracking(): ParseErrorList {
            return ParseErrorList(0, 0)
        }

        fun tracking(maxSize: Int): ParseErrorList {
            return ParseErrorList(INITIAL_CAPACITY, maxSize)
        }
    }
}
