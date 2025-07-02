package io.noties.markwon.editor

import android.text.Spanned

/**
 * @since 4.2.0
 */
object MarkwonEditorUtils {
    fun extractSpans(
        spanned: Spanned,
        types: MutableCollection<Class<*>>
    ): MutableMap<Class<*>, MutableList<Any?>> {
        val spans = spanned.getSpans(0, spanned.length, Any::class.java)
        val map: MutableMap<Class<*>, MutableList<Any?>> =
            HashMap(3)

        var type: Class<*>?

        for (span in spans) {
            type = span.javaClass
            if (types.contains(type)) {
                var list = map[type]
                if (list == null) {
                    list = ArrayList(3)
                    map.put(type, list)
                }
                list.add(span)
            }
        }

        return map
    }

    fun findDelimited(input: String, startFrom: Int, delimiter: String): Match? {
        val start = input.indexOf(delimiter, startFrom)
        if (start > -1) {
            val length = delimiter.length
            val end = input.indexOf(delimiter, start + length)
            if (end > -1) {
                return MatchImpl(delimiter, start, end + length)
            }
        }
        return null
    }

    fun findDelimited(
        input: String,
        start: Int,
        delimiter1: String,
        delimiter2: String
    ): Match? {
        val l1 = delimiter1.length
        val l2 = delimiter2.length

        val c1 = delimiter1[0]
        val c2 = delimiter2[0]

        var c: Char
        var previousC = 0.toChar()

        var match: Match?

        var i = start
        val length = input.length
        while (i < length) {
            c = input[i]

            // if this char is the same as previous (and we obviously have no match) -> skip
            if (c == previousC) {
                i++
                continue
            }

            if (c == c1) {
                match = matchDelimiter(input, i, length, delimiter1, l1)
                if (match != null) {
                    return match
                }
            } else if (c == c2) {
                match = matchDelimiter(input, i, length, delimiter2, l2)
                if (match != null) {
                    return match
                }
            }

            previousC = c
            i++
        }

        return null
    }

    // This method assumes that first char is matched already
    private fun matchDelimiter(
        input: String,
        start: Int,
        length: Int,
        delimiter: String,
        delimiterLength: Int
    ): Match? {
        if (start + delimiterLength < length) {
            var result = true

            for (i in 1..<delimiterLength) {
                if (input[start + i] != delimiter[i]) {
                    result = false
                    break
                }
            }

            if (result) {
                // find end
                val end = input.indexOf(delimiter, start + delimiterLength)
                // it's important to check if match has content
                if (end > -1 && (end - start) > delimiterLength) {
                    return MatchImpl(delimiter, start, end + delimiterLength)
                }
            }
        }

        return null
    }

    interface Match {
        fun delimiter(): String

        fun start(): Int

        fun end(): Int
    }

    private class MatchImpl(
        private val delimiter: String,
        private val start: Int,
        private val end: Int
    ) : Match {
        override fun delimiter(): String {
            return delimiter
        }

        override fun start(): Int {
            return start
        }

        override fun end(): Int {
            return end
        }

        override fun toString(): String {
            return "MatchImpl{" +
                    "delimiter='" + delimiter + '\'' +
                    ", start=" + start +
                    ", end=" + end +
                    '}'
        }
    }
}
