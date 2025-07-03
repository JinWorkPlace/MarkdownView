package io.noties.markwon.html

import java.io.IOException

internal object AppendableUtils {
    @JvmStatic
    fun appendQuietly(appendable: Appendable, c: Char) {
        try {
            appendable.append(c)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun appendQuietly(appendable: Appendable, cs: CharSequence) {
        try {
            appendable.append(cs)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun appendQuietly(appendable: Appendable, cs: CharSequence, start: Int, end: Int) {
        try {
            appendable.append(cs, start, end)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
