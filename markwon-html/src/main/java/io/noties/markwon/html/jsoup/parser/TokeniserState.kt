package io.noties.markwon.html.jsoup.parser

import io.noties.markwon.html.jsoup.nodes.DocumentType

/**
 * States and transition activations for the Tokeniser.
 */
enum class TokeniserState {
    Data {
        // in data state, gather characters until a character reference or tag is found
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.current()) {
                '&' -> t.advanceTransition(CharacterReferenceInData)
                '<' -> t.advanceTransition(TagOpen)
                NULL_CHAR -> {
                    t.error(this) // NOT replacement character (oddly?)
                    t.emit(r.consume())
                }

                Companion.EOF -> t.emit(Token.EOF())
                else -> {
                    val data = r.consumeData()
                    t.emit(data)
                }
            }
        }
    },
    CharacterReferenceInData {
        // from & in data
        override fun read(t: Tokeniser, r: CharacterReader) {
            Companion.readCharRef(t, Data)
        }
    },
    Rcdata {
        /** handles data in title, textarea etc */
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.current()) {
                '&' -> t.advanceTransition(CharacterReferenceInRcdata)
                '<' -> t.advanceTransition(RcdataLessthanSign)
                NULL_CHAR -> {
                    t.error(this)
                    r.advance()
                    t.emit(Companion.REPLACEMENT_CHAR)
                }

                Companion.EOF -> t.emit(Token.EOF())
                else -> {
                    val data = r.consumeToAny('&', '<', NULL_CHAR)
                    t.emit(data)
                }
            }
        }
    },
    CharacterReferenceInRcdata {
        override fun read(t: Tokeniser, r: CharacterReader) {
            Companion.readCharRef(t, Rcdata)
        }
    },
    Rawtext {
        override fun read(t: Tokeniser, r: CharacterReader) {
            Companion.readData(t, r, this, RawtextLessthanSign)
        }
    },
    ScriptData {
        override fun read(t: Tokeniser, r: CharacterReader) {
            Companion.readData(t, r, this, ScriptDataLessthanSign)
        }
    },
    PLAINTEXT {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.current()) {
                NULL_CHAR -> {
                    t.error(this)
                    r.advance()
                    t.emit(Companion.REPLACEMENT_CHAR)
                }

                Companion.EOF -> t.emit(Token.EOF())
                else -> {
                    val data = r.consumeTo(NULL_CHAR)
                    t.emit(data)
                }
            }
        }
    },
    TagOpen {
        // from < in data
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.current()) {
                '!' -> t.advanceTransition(MarkupDeclarationOpen)
                '/' -> t.advanceTransition(EndTagOpen)
                '?' -> t.advanceTransition(BogusComment)
                else -> if (r.matchesLetter()) {
                    t.createTagPending(true)
                    t.transition(TagName)
                } else {
                    t.error(this)
                    t.emit('<') // char that got us here
                    t.transition(Data)
                }
            }
        }
    },
    EndTagOpen {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.isEmpty) {
                t.eofError(this)
                t.emit("</")
                t.transition(Data)
            } else if (r.matchesLetter()) {
                t.createTagPending(false)
                t.transition(TagName)
            } else if (r.matches('>')) {
                t.error(this)
                t.advanceTransition(Data)
            } else {
                t.error(this)
                t.advanceTransition(BogusComment)
            }
        }
    },
    TagName {
        // from < or </ in data, will have start or end tag pending
        override fun read(t: Tokeniser, r: CharacterReader) {
            // previous TagOpen state did NOT consume, will have a letter char in current
            //String tagName = r.consumeToAnySorted(tagCharsSorted).toLowerCase();
            val tagName = r.consumeTagName()
            t.tagPending?.appendTagName(tagName)

            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeAttributeName)
                '/' -> t.transition(SelfClosingStartTag)
                '>' -> {
                    t.emitTagPending()
                    t.transition(Data)
                }

                NULL_CHAR -> t.tagPending?.appendTagName(Companion.REPLACEMENT_CHAR_STRING)
                Companion.EOF -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> t.tagPending?.appendTagName(c)
            }
        }
    },
    RcdataLessthanSign {
        // from < in rcdata
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matches('/')) {
                t.createTempBuffer()
                t.advanceTransition(RCDATAEndTagOpen)
            } else if (r.matchesLetter() && t.appropriateEndTagName() != null && !r.containsIgnoreCase(
                    "</" + t.appropriateEndTagName()
                )
            ) {
                // diverge from spec: got a start tag, but there's no appropriate end tag (</title>), so rather than
                // consuming to EOF; break out here
                t.tagPending = t.createTagPending(false).name(t.appropriateEndTagName())
                t.emitTagPending()
                r.unconsume() // undo "<"
                t.transition(Data)
            } else {
                t.emit("<")
                t.transition(Rcdata)
            }
        }
    },
    RCDATAEndTagOpen {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchesLetter()) {
                t.createTagPending(false)
                t.tagPending?.appendTagName(r.current())
                t.dataBuffer.append(r.current())
                t.advanceTransition(RCDATAEndTagName)
            } else {
                t.emit("</")
                t.transition(Rcdata)
            }
        }
    },
    RCDATAEndTagName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchesLetter()) {
                val name = r.consumeLetterSequence()
                t.tagPending?.appendTagName(name)
                t.dataBuffer.append(name)
                return
            }

            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> if (t.isAppropriateEndTagToken) t.transition(
                    BeforeAttributeName
                )
                else anythingElse(t, r)

                '/' -> if (t.isAppropriateEndTagToken) t.transition(SelfClosingStartTag)
                else anythingElse(t, r)

                '>' -> if (t.isAppropriateEndTagToken) {
                    t.emitTagPending()
                    t.transition(Data)
                } else anythingElse(t, r)

                else -> anythingElse(t, r)
            }
        }

        private fun anythingElse(t: Tokeniser, r: CharacterReader) {
            t.emit("</" + t.dataBuffer.toString())
            r.unconsume()
            t.transition(Rcdata)
        }
    },
    RawtextLessthanSign {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matches('/')) {
                t.createTempBuffer()
                t.advanceTransition(RawtextEndTagOpen)
            } else {
                t.emit('<')
                t.transition(Rawtext)
            }
        }
    },
    RawtextEndTagOpen {
        override fun read(t: Tokeniser, r: CharacterReader) {
            Companion.readEndTag(
                t,
                r,
                RawtextEndTagName,
                Rawtext
            )
        }
    },
    RawtextEndTagName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            Companion.handleDataEndTag(t, r, Rawtext)
        }
    },
    ScriptDataLessthanSign {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '/' -> {
                    t.createTempBuffer()
                    t.transition(ScriptDataEndTagOpen)
                }

                '!' -> {
                    t.emit("<!")
                    t.transition(ScriptDataEscapeStart)
                }

                else -> {
                    t.emit("<")
                    r.unconsume()
                    t.transition(ScriptData)
                }
            }
        }
    },
    ScriptDataEndTagOpen {
        override fun read(t: Tokeniser, r: CharacterReader) {
            Companion.readEndTag(
                t,
                r,
                ScriptDataEndTagName,
                ScriptData
            )
        }
    },
    ScriptDataEndTagName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            Companion.handleDataEndTag(t, r, ScriptData)
        }
    },
    ScriptDataEscapeStart {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matches('-')) {
                t.emit('-')
                t.advanceTransition(ScriptDataEscapeStartDash)
            } else {
                t.transition(ScriptData)
            }
        }
    },
    ScriptDataEscapeStartDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matches('-')) {
                t.emit('-')
                t.advanceTransition(ScriptDataEscapedDashDash)
            } else {
                t.transition(ScriptData)
            }
        }
    },
    ScriptDataEscaped {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.isEmpty) {
                t.eofError(this)
                t.transition(Data)
                return
            }

            when (r.current()) {
                '-' -> {
                    t.emit('-')
                    t.advanceTransition(ScriptDataEscapedDash)
                }

                '<' -> t.advanceTransition(ScriptDataEscapedLessthanSign)
                NULL_CHAR -> {
                    t.error(this)
                    r.advance()
                    t.emit(Companion.REPLACEMENT_CHAR)
                }

                else -> {
                    val data = r.consumeToAny('-', '<', NULL_CHAR)
                    t.emit(data)
                }
            }
        }
    },
    ScriptDataEscapedDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.isEmpty) {
                t.eofError(this)
                t.transition(Data)
                return
            }

            val c = r.consume()
            when (c) {
                '-' -> {
                    t.emit(c)
                    t.transition(ScriptDataEscapedDashDash)
                }

                '<' -> t.transition(ScriptDataEscapedLessthanSign)
                NULL_CHAR -> {
                    t.error(this)
                    t.emit(Companion.REPLACEMENT_CHAR)
                    t.transition(ScriptDataEscaped)
                }

                else -> {
                    t.emit(c)
                    t.transition(ScriptDataEscaped)
                }
            }
        }
    },
    ScriptDataEscapedDashDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.isEmpty) {
                t.eofError(this)
                t.transition(Data)
                return
            }

            val c = r.consume()
            when (c) {
                '-' -> t.emit(c)
                '<' -> t.transition(ScriptDataEscapedLessthanSign)
                '>' -> {
                    t.emit(c)
                    t.transition(ScriptData)
                }

                NULL_CHAR -> {
                    t.error(this)
                    t.emit(Companion.REPLACEMENT_CHAR)
                    t.transition(ScriptDataEscaped)
                }

                else -> {
                    t.emit(c)
                    t.transition(ScriptDataEscaped)
                }
            }
        }
    },
    ScriptDataEscapedLessthanSign {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchesLetter()) {
                t.createTempBuffer()
                t.dataBuffer.append(r.current())
                t.emit("<" + r.current())
                t.advanceTransition(ScriptDataDoubleEscapeStart)
            } else if (r.matches('/')) {
                t.createTempBuffer()
                t.advanceTransition(ScriptDataEscapedEndTagOpen)
            } else {
                t.emit('<')
                t.transition(ScriptDataEscaped)
            }
        }
    },
    ScriptDataEscapedEndTagOpen {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchesLetter()) {
                t.createTagPending(false)
                t.tagPending?.appendTagName(r.current())
                t.dataBuffer.append(r.current())
                t.advanceTransition(ScriptDataEscapedEndTagName)
            } else {
                t.emit("</")
                t.transition(ScriptDataEscaped)
            }
        }
    },
    ScriptDataEscapedEndTagName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            Companion.handleDataEndTag(t, r, ScriptDataEscaped)
        }
    },
    ScriptDataDoubleEscapeStart {
        override fun read(t: Tokeniser, r: CharacterReader) {
            Companion.handleDataDoubleEscapeTag(
                t,
                r,
                ScriptDataDoubleEscaped,
                ScriptDataEscaped
            )
        }
    },
    ScriptDataDoubleEscaped {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.current()
            when (c) {
                '-' -> {
                    t.emit(c)
                    t.advanceTransition(ScriptDataDoubleEscapedDash)
                }

                '<' -> {
                    t.emit(c)
                    t.advanceTransition(ScriptDataDoubleEscapedLessthanSign)
                }

                NULL_CHAR -> {
                    t.error(this)
                    r.advance()
                    t.emit(Companion.REPLACEMENT_CHAR)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> {
                    val data = r.consumeToAny('-', '<', NULL_CHAR)
                    t.emit(data)
                }
            }
        }
    },
    ScriptDataDoubleEscapedDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '-' -> {
                    t.emit(c)
                    t.transition(ScriptDataDoubleEscapedDashDash)
                }

                '<' -> {
                    t.emit(c)
                    t.transition(ScriptDataDoubleEscapedLessthanSign)
                }

                NULL_CHAR -> {
                    t.error(this)
                    t.emit(Companion.REPLACEMENT_CHAR)
                    t.transition(ScriptDataDoubleEscaped)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> {
                    t.emit(c)
                    t.transition(ScriptDataDoubleEscaped)
                }
            }
        }
    },
    ScriptDataDoubleEscapedDashDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '-' -> t.emit(c)
                '<' -> {
                    t.emit(c)
                    t.transition(ScriptDataDoubleEscapedLessthanSign)
                }

                '>' -> {
                    t.emit(c)
                    t.transition(ScriptData)
                }

                NULL_CHAR -> {
                    t.error(this)
                    t.emit(Companion.REPLACEMENT_CHAR)
                    t.transition(ScriptDataDoubleEscaped)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> {
                    t.emit(c)
                    t.transition(ScriptDataDoubleEscaped)
                }
            }
        }
    },
    ScriptDataDoubleEscapedLessthanSign {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matches('/')) {
                t.emit('/')
                t.createTempBuffer()
                t.advanceTransition(ScriptDataDoubleEscapeEnd)
            } else {
                t.transition(ScriptDataDoubleEscaped)
            }
        }
    },
    ScriptDataDoubleEscapeEnd {
        override fun read(t: Tokeniser, r: CharacterReader) {
            Companion.handleDataDoubleEscapeTag(
                t,
                r,
                ScriptDataEscaped,
                ScriptDataDoubleEscaped
            )
        }
    },
    BeforeAttributeName {
        // from tagname <xxx
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '/' -> t.transition(SelfClosingStartTag)
                '>' -> {
                    t.emitTagPending()
                    t.transition(Data)
                }

                NULL_CHAR -> {
                    t.error(this)
                    t.tagPending?.newAttribute()
                    r.unconsume()
                    t.transition(AttributeName)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                '"', '\'', '<', '=' -> {
                    t.error(this)
                    t.tagPending?.newAttribute()
                    t.tagPending?.appendAttributeName(c)
                    t.transition(AttributeName)
                }

                else -> {
                    t.tagPending?.newAttribute()
                    r.unconsume()
                    t.transition(AttributeName)
                }
            }
        }
    },
    AttributeName {
        // from before attribute name
        override fun read(t: Tokeniser, r: CharacterReader) {
            val name = r.consumeToAnySorted(*Companion.attributeNameCharsSorted)
            t.tagPending?.appendAttributeName(name)

            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(AfterAttributeName)
                '/' -> t.transition(SelfClosingStartTag)
                '=' -> t.transition(BeforeAttributeValue)
                '>' -> {
                    t.emitTagPending()
                    t.transition(Data)
                }

                NULL_CHAR -> {
                    t.error(this)
                    t.tagPending?.appendAttributeName(Companion.REPLACEMENT_CHAR)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                '"', '\'', '<' -> {
                    t.error(this)
                    t.tagPending?.appendAttributeName(c)
                }

                else -> t.tagPending?.appendAttributeName(c)
            }
        }
    },
    AfterAttributeName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '/' -> t.transition(SelfClosingStartTag)
                '=' -> t.transition(BeforeAttributeValue)
                '>' -> {
                    t.emitTagPending()
                    t.transition(Data)
                }

                NULL_CHAR -> {
                    t.error(this)
                    t.tagPending?.appendAttributeName(Companion.REPLACEMENT_CHAR)
                    t.transition(AttributeName)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                '"', '\'', '<' -> {
                    t.error(this)
                    t.tagPending?.newAttribute()
                    t.tagPending?.appendAttributeName(c)
                    t.transition(AttributeName)
                }

                else -> {
                    t.tagPending?.newAttribute()
                    r.unconsume()
                    t.transition(AttributeName)
                }
            }
        }
    },
    BeforeAttributeValue {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '"' -> t.transition(AttributeValue_doubleQuoted)
                '&' -> {
                    r.unconsume()
                    t.transition(AttributeValue_unquoted)
                }

                '\'' -> t.transition(AttributeValue_singleQuoted)
                NULL_CHAR -> {
                    t.error(this)
                    t.tagPending?.appendAttributeValue(Companion.REPLACEMENT_CHAR)
                    t.transition(AttributeValue_unquoted)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.emitTagPending()
                    t.transition(Data)
                }

                '>' -> {
                    t.error(this)
                    t.emitTagPending()
                    t.transition(Data)
                }

                '<', '=', '`' -> {
                    t.error(this)
                    t.tagPending?.appendAttributeValue(c)
                    t.transition(AttributeValue_unquoted)
                }

                else -> {
                    r.unconsume()
                    t.transition(AttributeValue_unquoted)
                }
            }
        }
    },
    AttributeValue_doubleQuoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val value = r.consumeToAny(*Companion.attributeDoubleValueCharsSorted)
            if (value.isNotEmpty()) t.tagPending?.appendAttributeValue(value)
            else t.tagPending?.setEmptyAttributeValue()

            val c = r.consume()
            when (c) {
                '"' -> t.transition(AfterAttributeValue_quoted)
                '&' -> {
                    val ref = t.consumeCharacterReference('"', true)
                    if (ref != null) t.tagPending?.appendAttributeValue(ref)
                    else t.tagPending?.appendAttributeValue('&')
                }

                NULL_CHAR -> {
                    t.error(this)
                    t.tagPending?.appendAttributeValue(Companion.REPLACEMENT_CHAR)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> t.tagPending?.appendAttributeValue(c)
            }
        }
    },
    AttributeValue_singleQuoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val value = r.consumeToAny(*Companion.attributeSingleValueCharsSorted)
            if (value.isNotEmpty()) t.tagPending?.appendAttributeValue(value)
            else t.tagPending?.setEmptyAttributeValue()

            val c = r.consume()
            when (c) {
                '\'' -> t.transition(AfterAttributeValue_quoted)
                '&' -> {
                    val ref = t.consumeCharacterReference('\'', true)
                    if (ref != null) t.tagPending?.appendAttributeValue(ref)
                    else t.tagPending?.appendAttributeValue('&')
                }

                NULL_CHAR -> {
                    t.error(this)
                    t.tagPending?.appendAttributeValue(Companion.REPLACEMENT_CHAR)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> t.tagPending?.appendAttributeValue(c)
            }
        }
    },
    AttributeValue_unquoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val value = r.consumeToAnySorted(*Companion.attributeValueUnquoted)
            if (value.isNotEmpty()) t.tagPending?.appendAttributeValue(value)

            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeAttributeName)
                '&' -> {
                    val ref = t.consumeCharacterReference('>', true)
                    if (ref != null) t.tagPending?.appendAttributeValue(ref)
                    else t.tagPending?.appendAttributeValue('&')
                }

                '>' -> {
                    t.emitTagPending()
                    t.transition(Data)
                }

                NULL_CHAR -> {
                    t.error(this)
                    t.tagPending?.appendAttributeValue(Companion.REPLACEMENT_CHAR)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                '"', '\'', '<', '=', '`' -> {
                    t.error(this)
                    t.tagPending?.appendAttributeValue(c)
                }

                else -> t.tagPending?.appendAttributeValue(c)
            }
        }
    },

    // CharacterReferenceInAttributeValue state handled inline
    AfterAttributeValue_quoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeAttributeName)
                '/' -> t.transition(SelfClosingStartTag)
                '>' -> {
                    t.emitTagPending()
                    t.transition(Data)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    r.unconsume()
                    t.transition(BeforeAttributeName)
                }
            }
        }
    },
    SelfClosingStartTag {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '>' -> {
                    t.tagPending?.isSelfClosing = true
                    t.emitTagPending()
                    t.transition(Data)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    r.unconsume()
                    t.transition(BeforeAttributeName)
                }
            }
        }
    },
    BogusComment {
        override fun read(t: Tokeniser, r: CharacterReader) {
            // todo: handle bogus comment starting from eof. when does that trigger?
            // rewind to capture character that lead us here
            r.unconsume()
            val comment = Token.Comment()
            comment.bogus = true
            comment.data.append(r.consumeTo('>'))
            // todo: replace nullChar with replaceChar
            t.emit(comment)
            t.advanceTransition(Data)
        }
    },
    MarkupDeclarationOpen {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchConsume("--")) {
                t.createCommentPending()
                t.transition(CommentStart)
            } else if (r.matchConsumeIgnoreCase("DOCTYPE")) {
                t.transition(Doctype)
            } else if (r.matchConsume("[CDATA[")) {
                // todo: should actually check current namepspace, and only non-html allows cdata. until namespace
                // is implemented properly, keep handling as cdata
                //} else if (!t.currentNodeInHtmlNS() && r.matchConsume("[CDATA[")) {
                t.createTempBuffer()
                t.transition(CdataSection)
            } else {
                t.error(this)
                t.advanceTransition(BogusComment) // advance so this character gets in bogus comment data's rewind
            }
        }
    },
    CommentStart {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '-' -> t.transition(CommentStartDash)
                NULL_CHAR -> {
                    t.error(this)
                    t.commentPending.data.append(Companion.REPLACEMENT_CHAR)
                    t.transition(Comment)
                }

                '>' -> {
                    t.error(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                else -> {
                    t.commentPending.data.append(c)
                    t.transition(Comment)
                }
            }
        }
    },
    CommentStartDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '-' -> t.transition(CommentStartDash)
                NULL_CHAR -> {
                    t.error(this)
                    t.commentPending.data.append(Companion.REPLACEMENT_CHAR)
                    t.transition(Comment)
                }

                '>' -> {
                    t.error(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                else -> {
                    t.commentPending.data.append(c)
                    t.transition(Comment)
                }
            }
        }
    },
    Comment {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.current()
            when (c) {
                '-' -> t.advanceTransition(CommentEndDash)
                NULL_CHAR -> {
                    t.error(this)
                    r.advance()
                    t.commentPending.data.append(Companion.REPLACEMENT_CHAR)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                else -> t.commentPending.data.append(
                    r.consumeToAny(
                        '-',
                        NULL_CHAR
                    )
                )
            }
        }
    },
    CommentEndDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '-' -> t.transition(CommentEnd)
                NULL_CHAR -> {
                    t.error(this)
                    t.commentPending.data.append('-')
                        .append(Companion.REPLACEMENT_CHAR)
                    t.transition(Comment)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                else -> {
                    t.commentPending.data.append('-').append(c)
                    t.transition(Comment)
                }
            }
        }
    },
    CommentEnd {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '>' -> {
                    t.emitCommentPending()
                    t.transition(Data)
                }

                NULL_CHAR -> {
                    t.error(this)
                    t.commentPending.data.append("--")
                        .append(Companion.REPLACEMENT_CHAR)
                    t.transition(Comment)
                }

                '!' -> {
                    t.error(this)
                    t.transition(CommentEndBang)
                }

                '-' -> {
                    t.error(this)
                    t.commentPending.data.append('-')
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.commentPending.data.append("--").append(c)
                    t.transition(Comment)
                }
            }
        }
    },
    CommentEndBang {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '-' -> {
                    t.commentPending.data.append("--!")
                    t.transition(CommentEndDash)
                }

                '>' -> {
                    t.emitCommentPending()
                    t.transition(Data)
                }

                NULL_CHAR -> {
                    t.error(this)
                    t.commentPending.data.append("--!")
                        .append(Companion.REPLACEMENT_CHAR)
                    t.transition(Comment)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                else -> {
                    t.commentPending.data.append("--!").append(c)
                    t.transition(Comment)
                }
            }
        }
    },
    Doctype {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeDoctypeName)
                Companion.EOF -> {
                    t.eofError(this)
                    t.error(this)
                    t.createDoctypePending()
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                '>' -> {
                    t.error(this)
                    t.createDoctypePending()
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.transition(BeforeDoctypeName)
                }
            }
        }
    },
    BeforeDoctypeName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchesLetter()) {
                t.createDoctypePending()
                t.transition(DoctypeName)
                return
            }
            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                NULL_CHAR -> {
                    t.error(this)
                    t.createDoctypePending()
                    t.doctypePending.name.append(Companion.REPLACEMENT_CHAR)
                    t.transition(DoctypeName)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.createDoctypePending()
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.createDoctypePending()
                    t.doctypePending.name.append(c)
                    t.transition(DoctypeName)
                }
            }
        }
    },
    DoctypeName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchesLetter()) {
                val name = r.consumeLetterSequence()
                t.doctypePending.name.append(name)
                return
            }
            val c = r.consume()
            when (c) {
                '>' -> {
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(AfterDoctypeName)
                NULL_CHAR -> {
                    t.error(this)
                    t.doctypePending.name.append(Companion.REPLACEMENT_CHAR)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> t.doctypePending.name.append(c)
            }
        }
    },
    AfterDoctypeName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.isEmpty) {
                t.eofError(this)
                t.doctypePending.isForceQuirks = true
                t.emitDoctypePending()
                t.transition(Data)
                return
            }
            if (r.matchesAny('\t', '\n', '\r', '\u000c', ' ')) r.advance() // ignore whitespace
            else if (r.matches('>')) {
                t.emitDoctypePending()
                t.advanceTransition(Data)
            } else if (r.matchConsumeIgnoreCase(DocumentType.PUBLIC_KEY)) {
                t.doctypePending.pubSysKey = DocumentType.PUBLIC_KEY
                t.transition(AfterDoctypePublicKeyword)
            } else if (r.matchConsumeIgnoreCase(DocumentType.SYSTEM_KEY)) {
                t.doctypePending.pubSysKey = DocumentType.SYSTEM_KEY
                t.transition(AfterDoctypeSystemKeyword)
            } else {
                t.error(this)
                t.doctypePending.isForceQuirks = true
                t.advanceTransition(BogusDoctype)
            }
        }
    },
    AfterDoctypePublicKeyword {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeDoctypePublicIdentifier)
                '"' -> {
                    t.error(this)
                    // set public id to empty string
                    t.transition(DoctypePublicIdentifier_doubleQuoted)
                }

                '\'' -> {
                    t.error(this)
                    // set public id to empty string
                    t.transition(DoctypePublicIdentifier_singleQuoted)
                }

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.transition(BogusDoctype)
                }
            }
        }
    },
    BeforeDoctypePublicIdentifier {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '"' ->                     // set public id to empty string
                    t.transition(DoctypePublicIdentifier_doubleQuoted)

                '\'' ->                     // set public id to empty string
                    t.transition(DoctypePublicIdentifier_singleQuoted)

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.transition(BogusDoctype)
                }
            }
        }
    },
    DoctypePublicIdentifier_doubleQuoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '"' -> t.transition(AfterDoctypePublicIdentifier)
                NULL_CHAR -> {
                    t.error(this)
                    t.doctypePending.publicIdentifier.append(Companion.REPLACEMENT_CHAR)
                }

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> t.doctypePending.publicIdentifier.append(c)
            }
        }
    },
    DoctypePublicIdentifier_singleQuoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\'' -> t.transition(AfterDoctypePublicIdentifier)
                NULL_CHAR -> {
                    t.error(this)
                    t.doctypePending.publicIdentifier.append(Companion.REPLACEMENT_CHAR)
                }

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> t.doctypePending.publicIdentifier.append(c)
            }
        }
    },
    AfterDoctypePublicIdentifier {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(
                    BetweenDoctypePublicAndSystemIdentifiers
                )

                '>' -> {
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                '"' -> {
                    t.error(this)
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_doubleQuoted)
                }

                '\'' -> {
                    t.error(this)
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_singleQuoted)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.transition(BogusDoctype)
                }
            }
        }
    },
    BetweenDoctypePublicAndSystemIdentifiers {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '>' -> {
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                '"' -> {
                    t.error(this)
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_doubleQuoted)
                }

                '\'' -> {
                    t.error(this)
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_singleQuoted)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.transition(BogusDoctype)
                }
            }
        }
    },
    AfterDoctypeSystemKeyword {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeDoctypeSystemIdentifier)
                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                '"' -> {
                    t.error(this)
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_doubleQuoted)
                }

                '\'' -> {
                    t.error(this)
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_singleQuoted)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                }
            }
        }
    },
    BeforeDoctypeSystemIdentifier {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '"' ->                     // set system id to empty string
                    t.transition(DoctypeSystemIdentifier_doubleQuoted)

                '\'' ->                     // set public id to empty string
                    t.transition(DoctypeSystemIdentifier_singleQuoted)

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.transition(BogusDoctype)
                }
            }
        }
    },
    DoctypeSystemIdentifier_doubleQuoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '"' -> t.transition(AfterDoctypeSystemIdentifier)
                NULL_CHAR -> {
                    t.error(this)
                    t.doctypePending.systemIdentifier.append(Companion.REPLACEMENT_CHAR)
                }

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> t.doctypePending.systemIdentifier.append(c)
            }
        }
    },
    DoctypeSystemIdentifier_singleQuoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\'' -> t.transition(AfterDoctypeSystemIdentifier)
                NULL_CHAR -> {
                    t.error(this)
                    t.doctypePending.systemIdentifier.append(Companion.REPLACEMENT_CHAR)
                }

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> t.doctypePending.systemIdentifier.append(c)
            }
        }
    },
    AfterDoctypeSystemIdentifier {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '>' -> {
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                Companion.EOF -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.transition(BogusDoctype)
                }
            }
        }
    },
    BogusDoctype {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val c = r.consume()
            when (c) {
                '>' -> {
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                Companion.EOF -> {
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {}
            }
        }
    },
    CdataSection {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val data = r.consumeTo("]]>")
            t.dataBuffer.append(data)
            if (r.matchConsume("]]>") || r.isEmpty) {
                t.emit(Token.CData(t.dataBuffer.toString()))
                t.transition(Data)
            } // otherwise, buffer underrun, stay in data section
        }
    };


    abstract fun read(t: Tokeniser, r: CharacterReader)

    companion object {
        const val NULL_CHAR: Char = '\u0000'

        // char searches. must be sorted, used in inSorted. MUST update TokenisetStateTest if more arrays are added.
        val attributeSingleValueCharsSorted: CharArray = charArrayOf(NULL_CHAR, '&', '\'')
        val attributeDoubleValueCharsSorted: CharArray = charArrayOf(NULL_CHAR, '"', '&')
        val attributeNameCharsSorted: CharArray =
            charArrayOf(NULL_CHAR, '\t', '\n', '\u000c', '\r', ' ', '"', '\'', '/', '<', '=', '>')
        val attributeValueUnquoted: CharArray =
            charArrayOf(
                NULL_CHAR, '\t', '\n',
                '\u000c', '\r', ' ', '"', '&', '\'', '<', '=', '>', '`'
            )

        private const val REPLACEMENT_CHAR = Tokeniser.REPLACEMENT_CHARACTER
        private const val REPLACEMENT_CHAR_STRING = Tokeniser.REPLACEMENT_CHARACTER.toString()
        private const val EOF = CharacterReader.EOF

        /**
         * Handles RawtextEndTagName, ScriptDataEndTagName, and ScriptDataEscapedEndTagName. Same body impl, just
         * different else exit transitions.
         */
        private fun handleDataEndTag(
            t: Tokeniser,
            r: CharacterReader,
            elseTransition: TokeniserState
        ) {
            if (r.matchesLetter()) {
                val name = r.consumeLetterSequence()
                t.tagPending?.appendTagName(name)
                t.dataBuffer.append(name)
                return
            }

            var needsExitTransition = false
            if (t.isAppropriateEndTagToken && !r.isEmpty) {
                val c = r.consume()
                when (c) {
                    '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeAttributeName)
                    '/' -> t.transition(SelfClosingStartTag)
                    '>' -> {
                        t.emitTagPending()
                        t.transition(Data)
                    }

                    else -> {
                        t.dataBuffer.append(c)
                        needsExitTransition = true
                    }
                }
            } else {
                needsExitTransition = true
            }

            if (needsExitTransition) {
                t.emit("</" + t.dataBuffer.toString())
                t.transition(elseTransition)
            }
        }

        private fun readData(
            t: Tokeniser,
            r: CharacterReader,
            current: TokeniserState,
            advance: TokeniserState
        ) {
            when (r.current()) {
                '<' -> t.advanceTransition(advance)
                NULL_CHAR -> {
                    t.error(current)
                    r.advance()
                    t.emit(REPLACEMENT_CHAR)
                }

                EOF -> t.emit(Token.EOF())
                else -> {
                    val data = r.consumeToAny(
                        '<',
                        NULL_CHAR
                    ) // todo - why hunt for null here? Just consumeTo'<'?
                    t.emit(data)
                }
            }
        }

        private fun readCharRef(t: Tokeniser, advance: TokeniserState) {
            val c = t.consumeCharacterReference(null, false)
            if (c == null) t.emit('&')
            else t.emit(c)
            t.transition(advance)
        }

        private fun readEndTag(
            t: Tokeniser,
            r: CharacterReader,
            a: TokeniserState,
            b: TokeniserState
        ) {
            if (r.matchesLetter()) {
                t.createTagPending(false)
                t.transition(a)
            } else {
                t.emit("</")
                t.transition(b)
            }
        }

        private fun handleDataDoubleEscapeTag(
            t: Tokeniser,
            r: CharacterReader,
            primary: TokeniserState,
            fallback: TokeniserState
        ) {
            if (r.matchesLetter()) {
                val name = r.consumeLetterSequence()
                t.dataBuffer.append(name)
                t.emit(name)
                return
            }

            val c = r.consume()
            when (c) {
                '\t', '\n', '\r', '\u000c', ' ', '/', '>' -> {
                    if (t.dataBuffer.toString() == "script") t.transition(primary)
                    else t.transition(fallback)
                    t.emit(c)
                }

                else -> {
                    r.unconsume()
                    t.transition(fallback)
                }
            }
        }
    }
}
