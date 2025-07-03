package io.noties.markwon.html.jsoup.parser

import io.noties.markwon.html.jsoup.UncheckedIOException
import io.noties.markwon.html.jsoup.helper.Validate
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.util.Arrays
import kotlin.math.min

/**
 * CharacterReader consumes tokens off a string. Used internally by jsoup. API subject to changes.
 */
class CharacterReader @JvmOverloads constructor(input: Reader, sz: Int = MAX_BUFFER_LEN) {
    private val charBuf: CharArray
    private val reader: Reader
    private var bufLength = 0
    private var bufSplitPoint = 0
    private var bufPos = 0
    private var readerPos = 0
    private var bufMark = 0
    private val stringCache =
        arrayOfNulls<String>(128) // holds reused strings in this doc, to lessen garbage

    init {
        Validate.notNull(input)
        Validate.isTrue(input.markSupported())
        reader = input
        charBuf = CharArray(MAX_BUFFER_LEN)
        bufferUp()
    }

    constructor(input: String) : this(StringReader(input), input.length)

    //    public void swapInput(@NonNull String input) {
    //        reader = new StringReader(input);
    //        bufLength = 0;
    //        bufSplitPoint = 0;
    //        bufPos = 0;
    //        readerPos = 0;
    //        bufferUp();
    //    }
    private fun bufferUp() {
        if (bufPos < bufSplitPoint) return

        try {
            reader.skip(bufPos.toLong())
            reader.mark(MAX_BUFFER_LEN)
            val read = reader.read(charBuf)
            reader.reset()
            if (read != -1) {
                bufLength = read
                readerPos += bufPos
                bufPos = 0
                bufMark = 0
                bufSplitPoint = min(bufLength, READ_AHEAD_LIMIT)
            }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    /**
     * Gets the current cursor position in the content.
     *
     * @return current position
     */
    fun pos(): Int {
        return readerPos + bufPos
    }

    val isEmpty: Boolean
        /**
         * Tests if all the content has been read.
         *
         * @return true if nothing left to read.
         */
        get() {
            bufferUp()
            return bufPos >= bufLength
        }

    private val isEmptyNoBufferUp: Boolean
        get() = bufPos >= bufLength

    /**
     * Get the char at the current position.
     *
     * @return char
     */
    fun current(): Char {
        bufferUp()
        return if (this.isEmptyNoBufferUp) EOF else charBuf[bufPos]
    }

    fun consume(): Char {
        bufferUp()
        val `val` = if (this.isEmptyNoBufferUp) EOF else charBuf[bufPos]
        bufPos++
        return `val`
    }

    fun unconsume() {
        bufPos--
    }

    /**
     * Moves the current position by one.
     */
    fun advance() {
        bufPos++
    }

    fun mark() {
        bufMark = bufPos
    }

    fun rewindToMark() {
        bufPos = bufMark
    }

    /**
     * Returns the number of characters between the current position and the next instance of the input char
     *
     * @param c scan target
     * @return offset between current position and next instance of target. -1 if not found.
     */
    fun nextIndexOf(c: Char): Int {
        // doesn't handle scanning for surrogates
        bufferUp()
        for (i in bufPos..<bufLength) {
            if (c == charBuf[i]) return i - bufPos
        }
        return -1
    }

    /**
     * Returns the number of characters between the current position and the next instance of the input sequence
     *
     * @param seq scan target
     * @return offset between current position and next instance of target. -1 if not found.
     */
    fun nextIndexOf(seq: CharSequence): Int {
        bufferUp()
        // doesn't handle scanning for surrogates
        val startChar = seq[0]
        var offset = bufPos
        while (offset < bufLength) {
            // scan to first instance of start char:
            if (startChar != charBuf[offset]) while (++offset < bufLength && startChar != charBuf[offset]) { /* empty */
            }
            var i = offset + 1
            val last = i + seq.length - 1
            if (offset < bufLength && last <= bufLength) {
                var j = 1
                while (i < last && seq[j] == charBuf[i]) {
                    i++
                    j++
                }
                if (i == last)  // found full sequence
                    return offset - bufPos
            }
            offset++
        }
        return -1
    }

    /**
     * Reads characters up to the specific char.
     *
     * @param c the delimiter
     * @return the chars read
     */
    fun consumeTo(c: Char): String {
        val offset = nextIndexOf(c)
        if (offset != -1) {
            val consumed: String = cacheString(charBuf, stringCache, bufPos, offset)
            bufPos += offset
            return consumed
        } else {
            return consumeToEnd()
        }
    }

    fun consumeTo(seq: String): String {
        val offset = nextIndexOf(seq)
        if (offset != -1) {
            val consumed: String = cacheString(charBuf, stringCache, bufPos, offset)
            bufPos += offset
            return consumed
        } else {
            return consumeToEnd()
        }
    }

    /**
     * Read characters until the first of any delimiters is found.
     *
     * @param chars delimiters to scan for
     * @return characters read up to the matched delimiter.
     */
    fun consumeToAny(vararg chars: Char): String {
        bufferUp()
        val start = bufPos
        val remaining = bufLength

        OUTER@ while (bufPos < remaining) {
            for (c in chars) {
                if (charBuf[bufPos] == c) break@OUTER
            }
            bufPos++
        }

        return if (bufPos > start) cacheString(charBuf, stringCache, start, bufPos - start) else ""
    }

    fun consumeToAnySorted(vararg chars: Char): String {
        bufferUp()
        val start = bufPos
        val remaining = bufLength

        while (bufPos < remaining) {
            if (Arrays.binarySearch(chars, charBuf[bufPos]) >= 0) break
            bufPos++
        }

        return if (bufPos > start) cacheString(charBuf, stringCache, start, bufPos - start) else ""
    }

    fun consumeData(): String {
        // &, <, null
        bufferUp()
        val start = bufPos
        val remaining = bufLength

        while (bufPos < remaining) {
            val c = charBuf[bufPos]
            if (c == '&' || c == '<' || c == TokeniserState.NULL_CHAR) break
            bufPos++
        }

        return if (bufPos > start) cacheString(charBuf, stringCache, start, bufPos - start) else ""
    }

    fun consumeTagName(): String {
        // '\t', '\n', '\r', '\f', ' ', '/', '>', nullChar
        bufferUp()
        val start = bufPos
        val remaining = bufLength

        while (bufPos < remaining) {
            val c = charBuf[bufPos]
            if (c == '\t' || c == '\n' || c == '\r' || c == '\u000c' || c == ' ' || c == '/' || c == '>' || c == TokeniserState.NULL_CHAR) break
            bufPos++
        }

        return if (bufPos > start) cacheString(charBuf, stringCache, start, bufPos - start) else ""
    }

    fun consumeToEnd(): String {
        bufferUp()
        val data: String = cacheString(charBuf, stringCache, bufPos, bufLength - bufPos)
        bufPos = bufLength
        return data
    }

    fun consumeLetterSequence(): String {
        bufferUp()
        val start = bufPos
        while (bufPos < bufLength) {
            val c = charBuf[bufPos]
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || Character.isLetter(c)) bufPos++
            else break
        }

        return cacheString(charBuf, stringCache, start, bufPos - start)
    }

    fun consumeLetterThenDigitSequence(): String {
        bufferUp()
        val start = bufPos
        while (bufPos < bufLength) {
            val c = charBuf[bufPos]
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || Character.isLetter(c)) bufPos++
            else break
        }
        while (!this.isEmptyNoBufferUp) {
            val c = charBuf[bufPos]
            if (c >= '0' && c <= '9') bufPos++
            else break
        }

        return cacheString(charBuf, stringCache, start, bufPos - start)
    }

    fun consumeHexSequence(): String {
        bufferUp()
        val start = bufPos
        while (bufPos < bufLength) {
            val c = charBuf[bufPos]
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')) bufPos++
            else break
        }
        return cacheString(charBuf, stringCache, start, bufPos - start)
    }

    fun consumeDigitSequence(): String {
        bufferUp()
        val start = bufPos
        while (bufPos < bufLength) {
            val c = charBuf[bufPos]
            if (c >= '0' && c <= '9') bufPos++
            else break
        }
        return cacheString(charBuf, stringCache, start, bufPos - start)
    }

    fun matches(c: Char): Boolean {
        return !this.isEmpty && charBuf[bufPos] == c
    }

    fun matches(seq: String): Boolean {
        bufferUp()
        val scanLength = seq.length
        if (scanLength > bufLength - bufPos) return false

        for (offset in 0..<scanLength) if (seq[offset] != charBuf[bufPos + offset]) return false
        return true
    }

    fun matchesIgnoreCase(seq: String): Boolean {
        bufferUp()
        val scanLength = seq.length
        if (scanLength > bufLength - bufPos) return false

        for (offset in 0..<scanLength) {
            val upScan = seq[offset].uppercaseChar()
            val upTarget = charBuf[bufPos + offset].uppercaseChar()
            if (upScan != upTarget) return false
        }
        return true
    }

    fun matchesAny(vararg seq: Char): Boolean {
        if (this.isEmpty) return false

        bufferUp()
        val c = charBuf[bufPos]
        for (seek in seq) {
            if (seek == c) return true
        }
        return false
    }

    fun matchesAnySorted(seq: CharArray): Boolean {
        bufferUp()
        return !this.isEmpty && Arrays.binarySearch(seq, charBuf[bufPos]) >= 0
    }

    fun matchesLetter(): Boolean {
        if (this.isEmpty) return false
        val c = charBuf[bufPos]
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || Character.isLetter(c)
    }

    fun matchesDigit(): Boolean {
        if (this.isEmpty) return false
        val c = charBuf[bufPos]
        return (c >= '0' && c <= '9')
    }

    fun matchConsume(seq: String): Boolean {
        bufferUp()
        if (matches(seq)) {
            bufPos += seq.length
            return true
        } else {
            return false
        }
    }

    fun matchConsumeIgnoreCase(seq: String): Boolean {
        if (matchesIgnoreCase(seq)) {
            bufPos += seq.length
            return true
        } else {
            return false
        }
    }

    fun containsIgnoreCase(seq: String): Boolean {
        // used to check presence of </title>, </style>. only finds consistent case.
        val loScan = seq.lowercase()
        val hiScan = seq.uppercase()
        return (nextIndexOf(loScan) > -1) || (nextIndexOf(hiScan) > -1)
    }

    override fun toString(): String {
        return String(charBuf, bufPos, bufLength - bufPos)
    }

    // just used for testing
    fun rangeEquals(start: Int, count: Int, cached: String): Boolean {
        return rangeEquals(charBuf, start, count, cached)
    }

    companion object {
        const val EOF: Char = (-1).toChar()
        private const val MAX_STRING_CACHE_LEN = 12
        const val MAX_BUFFER_LEN: Int = 1024 * 4 // visible for testing
        private const val READ_AHEAD_LIMIT = (MAX_BUFFER_LEN * 0.75).toInt()

        /**
         * Caches short strings, as a flywheel pattern, to reduce GC load. Just for this doc, to prevent leaks.
         *
         *
         * Simplistic, and on hash collisions just falls back to creating a new string, vs a full HashMap with Entry list.
         * That saves both having to create objects as hash keys, and running through the entry list, at the expense of
         * some more duplicates.
         */
        private fun cacheString(
            charBuf: CharArray,
            stringCache: Array<String?>,
            start: Int,
            count: Int
        ): String {
            // limit (no cache):
            if (count > MAX_STRING_CACHE_LEN) return String(charBuf, start, count)
            if (count < 1) return ""

            // calculate hash:
            var hash = 0
            var offset = start
            for (i in 0..<count) {
                hash = 31 * hash + charBuf[offset++].code
            }

            // get from cache
            val index = hash and stringCache.size - 1
            var cached = stringCache[index]

            if (cached == null) { // miss, add
                cached = String(charBuf, start, count)
                stringCache[index] = cached
            } else { // hashcode hit, check equality
                if (rangeEquals(charBuf, start, count, cached)) { // hit
                    return cached
                } else { // hashcode conflict
                    cached = String(charBuf, start, count)
                    stringCache[index] =
                        cached // update the cache, as recently used strings are more likely to show up again
                }
            }
            return cached
        }

        /**
         * Check if the value of the provided range equals the string.
         */
        fun rangeEquals(charBuf: CharArray, start: Int, count: Int, cached: String): Boolean {
            var count = count
            if (count == cached.length) {
                var i = start
                var j = 0
                while (count-- != 0) {
                    if (charBuf[i++] != cached[j++]) return false
                }
                return true
            }
            return false
        }
    }
}
