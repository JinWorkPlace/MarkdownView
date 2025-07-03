package io.noties.markwon.html

import io.noties.markwon.html.AppendableUtils.appendQuietly

internal abstract class TrimmingAppender {
    abstract fun <T> append(output: T, data: String) where T : Appendable?, T : CharSequence?

    internal class Impl : TrimmingAppender() {
        // if data is fully empty (consists of white spaces) -> do not add anything
        // leading ws:
        //  - trim to one space (if at all present) append to output only if previous is ws
        // trailing ws:
        //  - if present trim to single space
        override fun <T> append(output: T, data: String) where T : Appendable?, T : CharSequence? {
            val startLength = output!!.length

            var c: Char

            var previousIsWhiteSpace = false

            var i = 0
            val length = data.length
            while (i < length) {
                c = data[i]

                if (Character.isWhitespace(c)) {
                    previousIsWhiteSpace = true
                    i++
                    continue
                }

                if (previousIsWhiteSpace) {
                    // validate that output has ws as last char
                    val outputLength = output.length
                    if (outputLength > 0 && !Character.isWhitespace(output[outputLength - 1])) {
                        appendQuietly(output, ' ')
                    }
                }

                previousIsWhiteSpace = false
                appendQuietly(output, c)
                i++
            }

            // additionally check if previousIsWhiteSpace is true (if data ended with ws)
            // BUT only if we have added something (otherwise the whole data is empty (white))
            if (previousIsWhiteSpace && (startLength < output.length)) {
                appendQuietly(output, ' ')
            }
        }
    }

    companion object {
        fun create(): TrimmingAppender {
            return Impl()
        }
    }
}
