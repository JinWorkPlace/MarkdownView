package io.noties.markwon.html.jsoup.parser

import io.noties.markwon.html.jsoup.helper.Validate
import io.noties.markwon.html.jsoup.nodes.CommonMarkEntities.codepointsForName
import io.noties.markwon.html.jsoup.nodes.CommonMarkEntities.isNamedEntity
import io.noties.markwon.html.jsoup.parser.Token.Companion.reset
import java.util.Arrays

/**
 * Readers the input stream into tokens.
 */
class Tokeniser(// html input
    private val reader: CharacterReader, // errors found while tokenising
    private val errors: ParseErrorList
) {
    var state: TokeniserState = TokeniserState.Data // current tokenisation state
        private set
    private var emitPending: Token? = null // the token we are about to emit on next read
    private var isEmitPending = false
    private var charsString: String? =
        null // characters pending an emit. Will fall to charsBuilder if more than one
    private val charsBuilder =
        StringBuilder(1024) // buffers characters to output as one token, if more than one emit per read
    var dataBuffer: StringBuilder = StringBuilder(1024) // buffers data looking for </script>

    var tagPending: Token.Tag? = null // tag we are building up
    var startPending: Token.StartTag = Token.StartTag()
    var endPending: Token.EndTag = Token.EndTag()
    var charPending: Token.Character = Token.Character()
    var doctypePending: Token.Doctype = Token.Doctype() // doctype building up
    var commentPending: Token.Comment = Token.Comment() // comment building up
    private var lastStartTag: String? =
        null // the last start tag emitted, to test appropriate end tag

    fun read(): Token? {
        while (!isEmitPending) state.read(this, reader)

        // if emit is pending, a non-character token was found: return any chars in buffer, and leave token for next read:
        if (charsBuilder.isNotEmpty()) {
            val str = charsBuilder.toString()
            charsBuilder.delete(0, charsBuilder.length)
            charsString = null
            return charPending.data(str)
        } else if (charsString != null) {
            val token: Token = charPending.data(charsString)
            charsString = null
            return token
        } else {
            isEmitPending = false
            return emitPending
        }
    }

    fun emit(token: Token) {
        Validate.isFalse(isEmitPending, "There is an unread token pending!")

        emitPending = token
        isEmitPending = true

        if (token.type == Token.TokenType.StartTag) {
            val startTag = token as Token.StartTag
            lastStartTag = startTag.tagName
        } else if (token.type == Token.TokenType.EndTag) {
            val endTag = token as Token.EndTag
            if (endTag.attributes != null) error("Attributes incorrectly present on end tag")
        }
    }

    fun emit(str: String?) {
        // buffer strings up until last string token found, to emit only one token for a run of character refs etc.
        // does not set isEmitPending; read checks that
        if (charsString == null) {
            charsString = str
        } else {
            if (charsBuilder.isEmpty()) { // switching to string builder as more than one emit before read
                charsBuilder.append(charsString)
            }
            charsBuilder.append(str)
        }
    }

    fun emit(chars: CharArray?) {
        emit(String(chars!!))
    }

    fun emit(codepoints: IntArray) {
        emit(String(codepoints, 0, codepoints.size))
    }

    fun emit(c: Char) {
        emit(c.toString())
    }

    fun transition(state: TokeniserState) {
        this.state = state
    }

    fun advanceTransition(state: TokeniserState) {
        reader.advance()
        this.state = state
    }

    private val codepointHolder = IntArray(1) // holder to not have to keep creating arrays
    private val multipointHolder = IntArray(2)

    fun consumeCharacterReference(
        additionalAllowedCharacter: Char?,
        inAttribute: Boolean
    ): IntArray? {
        if (reader.isEmpty) return null
        if (additionalAllowedCharacter != null && additionalAllowedCharacter == reader.current()) return null
        if (reader.matchesAnySorted(notCharRefCharsSorted)) return null

        val codeRef = codepointHolder
        reader.mark()
        if (reader.matchConsume("#")) { // numbered
            val isHexMode = reader.matchConsumeIgnoreCase("X")
            val numRef =
                if (isHexMode) reader.consumeHexSequence() else reader.consumeDigitSequence()
            if (numRef.isEmpty()) { // didn't match anything
                characterReferenceError("numeric reference with no numerals")
                reader.rewindToMark()
                return null
            }
            if (!reader.matchConsume(";")) characterReferenceError("missing semicolon") // missing semi

            var charval = -1
            try {
                val base = if (isHexMode) 16 else 10
                charval = numRef.toInt(base)
            } catch (ignored: NumberFormatException) {
            } // skip

            if (charval == -1 || (charval >= 0xD800 && charval <= 0xDFFF) || charval > 0x10FFFF) {
                characterReferenceError("character outside of valid range")
                codeRef[0] = REPLACEMENT_CHARACTER.code
                return codeRef
            } else {
                // fix illegal unicode characters to match browser behavior
                if (charval >= WIN_1252_EXTENSION_START && charval < WIN_1252_EXTENSION_START + win1252Extensions.size) {
                    characterReferenceError("character is not a valid unicode code point")
                    charval = win1252Extensions[charval - WIN_1252_EXTENSION_START]
                }

                // todo: implement number replacement table
                // todo: check for extra illegal unicode points as parse errors
                codeRef[0] = charval
                return codeRef
            }
        } else { // named
            // get as many letters as possible, and look for matching entities.
            val nameRef = reader.consumeLetterThenDigitSequence()
            val looksLegit = reader.matches(';')
            // found if a base named entity without a ;, or an extended entity with the ;.
            val found = (isNamedEntity(nameRef) && looksLegit)

            if (!found) {
                reader.rewindToMark()
                if (looksLegit)  // named with semicolon
                    characterReferenceError(String.format("invalid named referenece '%s'", nameRef))
                return null
            }
            if (inAttribute && (reader.matchesLetter() || reader.matchesDigit() || reader.matchesAny(
                    '=',
                    '-',
                    '_'
                ))
            ) {
                // don't want that to match
                reader.rewindToMark()
                return null
            }
            if (!reader.matchConsume(";")) characterReferenceError("missing semicolon") // missing semi

            val numChars = codepointsForName(nameRef, multipointHolder)
            when (numChars) {
                1 -> {
                    codeRef[0] = multipointHolder[0]
                    return codeRef
                }

                2 -> {
                    return multipointHolder
                }

                else -> {
                    Validate.fail("Unexpected characters returned for $nameRef")
                    return multipointHolder
                }
            }
        }
    }

    fun createTagPending(start: Boolean): Token.Tag {
        tagPending = if (start) startPending.reset() else endPending.reset()
        return tagPending!!
    }

    fun emitTagPending() {
        tagPending!!.finaliseTag()
        emit(tagPending!!)
    }

    fun createCommentPending() {
        commentPending.reset()
    }

    fun emitCommentPending() {
        emit(commentPending)
    }

    fun createDoctypePending() {
        doctypePending.reset()
    }

    fun emitDoctypePending() {
        emit(doctypePending)
    }

    fun createTempBuffer() {
        reset(dataBuffer)
    }

    val isAppropriateEndTagToken: Boolean
        get() = lastStartTag != null && tagPending!!.name().equals(lastStartTag, ignoreCase = true)

    fun appropriateEndTagName(): String? {
        return lastStartTag // could be null
    }

    fun error(state: TokeniserState?) {
        if (errors.canAddError()) errors.add(
            ParseError(
                reader.pos(),
                "Unexpected character '%s' in input state [%s]",
                reader.current(),
                state
            )
        )
    }

    fun eofError(state: TokeniserState?) {
        if (errors.canAddError()) errors.add(
            ParseError(
                reader.pos(),
                "Unexpectedly reached end of file (EOF) in input state [%s]",
                state
            )
        )
    }

    private fun characterReferenceError(message: String?) {
        if (errors.canAddError()) errors.add(
            ParseError(
                reader.pos(),
                "Invalid character reference: %s",
                message
            )
        )
    }

    fun error(errorMsg: String?) {
        if (errors.canAddError()) errors.add(ParseError(reader.pos(), errorMsg))
    }

    fun currentNodeInHtmlNS(): Boolean {
        return true
    } //    /**
    //     * Utility method to consume reader and unescape entities found within.
    //     * @param inAttribute if the text to be unescaped is in an attribute
    //     * @return unescaped string from reader
    //     */
    //    String unescapeEntities(boolean inAttribute) {
    //        StringBuilder builder = StringUtil.stringBuilder();
    //        while (!reader.isEmpty()) {
    //            builder.append(reader.consumeTo('&'));
    //            if (reader.matches('&')) {
    //                reader.consume();
    //                int[] c = consumeCharacterReference(null, inAttribute);
    //                if (c == null || c.length==0)
    //                    builder.append('&');
    //                else {
    //                    builder.appendCodePoint(c[0]);
    //                    if (c.length == 2)
    //                        builder.appendCodePoint(c[1]);
    //                }
    //
    //            }
    //        }
    //        return builder.toString();
    //    }

    companion object {
        const val REPLACEMENT_CHARACTER: Char = '\uFFFD' // replaces null character
        private val notCharRefCharsSorted = charArrayOf('\t', '\n', '\r', '\u000c', ' ', '<', '&')

        // Some illegal character escapes are parsed by browsers as windows-1252 instead. See issue #1034
        // https://html.spec.whatwg.org/multipage/parsing.html#numeric-character-reference-end-state
        const val WIN_1252_EXTENSION_START: Int = 0x80
        val win1252Extensions: IntArray = intArrayOf(
            // we could build this manually, but Windows-1252 is not a standard java charset so that could break on
            // some platforms - this table is verified with a test
            0x20AC,
            0x0081,
            0x201A,
            0x0192,
            0x201E,
            0x2026,
            0x2020,
            0x2021,
            0x02C6,
            0x2030,
            0x0160,
            0x2039,
            0x0152,
            0x008D,
            0x017D,
            0x008F,
            0x0090,
            0x2018,
            0x2019,
            0x201C,
            0x201D,
            0x2022,
            0x2013,
            0x2014,
            0x02DC,
            0x2122,
            0x0161,
            0x203A,
            0x0153,
            0x009D,
            0x017E,
            0x0178,
        )

        init {
            Arrays.sort(notCharRefCharsSorted)
        }
    }
}
