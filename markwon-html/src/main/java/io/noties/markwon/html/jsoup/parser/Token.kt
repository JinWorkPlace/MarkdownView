package io.noties.markwon.html.jsoup.parser

import io.noties.markwon.html.jsoup.helper.Normalizer.lowerCase
import io.noties.markwon.html.jsoup.helper.Validate
import io.noties.markwon.html.jsoup.nodes.Attributes

/**
 * Parse tokens for the Tokeniser.
 */
abstract class Token protected constructor(@JvmField val type: TokenType) {
    //    String tokenType() {
    //        return this.getClass().getSimpleName();
    //    }
    /**
     * Reset the data represent by this token, for reuse. Prevents the need to create transfer objects for every
     * piece of data, which immediately get GCed.
     */
    abstract fun reset(): Token?

    class Doctype internal constructor() : Token(TokenType.Doctype) {
        val name: StringBuilder = StringBuilder()
        var pubSysKey: String? = null
        val publicIdentifier: StringBuilder = StringBuilder()
        val systemIdentifier: StringBuilder = StringBuilder()
        var isForceQuirks: Boolean = false

        override fun reset(): Token {
            reset(name)
            pubSysKey = null
            reset(publicIdentifier)
            reset(systemIdentifier)
            this.isForceQuirks = false
            return this
        }

        fun getName(): String {
            return name.toString()
        }

        fun getPublicIdentifier(): String {
            return publicIdentifier.toString()
        }

        fun getSystemIdentifier(): String {
            return systemIdentifier.toString()
        }
    }

    abstract class Tag protected constructor(tokenType: TokenType) : Token(tokenType) {
        @JvmField
        var tagName: String? = null

        @JvmField
        var normalName: String? = null // lc version of tag name, for case insensitive tree build
        private var pendingAttributeName: String? =
            null // attribute names are generally caught in one hop, not accumulated
        private val pendingAttributeValue =
            StringBuilder() // but values are accumulated, from e.g. & in hrefs
        private var pendingAttributeValueS: String? =
            null // try to get attr vals in one shot, vs Builder
        private var hasEmptyAttributeValue =
            false // distinguish boolean attribute from empty string value
        private var hasPendingAttributeValue = false
        var isSelfClosing: Boolean = false

        @JvmField
        var attributes: Attributes? =
            null // start tags get attributes on construction. End tags get attributes on first new attribute (but only for parser convenience, not used).

        override fun reset(): Tag {
            tagName = null
            normalName = null
            pendingAttributeName = null
            reset(pendingAttributeValue)
            pendingAttributeValueS = null
            hasEmptyAttributeValue = false
            hasPendingAttributeValue = false
            this.isSelfClosing = false
            attributes = null
            return this
        }

        fun newAttribute() {
            if (attributes == null) attributes = Attributes()

            if (pendingAttributeName != null) {
                // the tokeniser has skipped whitespace control chars, but trimming could collapse to empty for other control codes, so verify here
                pendingAttributeName = pendingAttributeName!!.trim { it <= ' ' }
                if (pendingAttributeName!!.isNotEmpty()) {
                    val value: String? =
                        if (hasPendingAttributeValue) if (pendingAttributeValue.isNotEmpty()) pendingAttributeValue.toString() else pendingAttributeValueS
                        else if (hasEmptyAttributeValue) ""
                        else null
                    attributes!!.put(pendingAttributeName!!, value)
                }
            }
            pendingAttributeName = null
            hasEmptyAttributeValue = false
            hasPendingAttributeValue = false
            reset(pendingAttributeValue)
            pendingAttributeValueS = null
        }

        fun finaliseTag() {
            // finalises for emit
            if (pendingAttributeName != null) {
                // todo: check if attribute name exists; if so, drop and error
                newAttribute()
            }
        }

        fun name(): String { // preserves case, for input into Tag.valueOf (which may drop case)
            Validate.isFalse(tagName == null || tagName!!.isEmpty())
            return tagName!!
        }

        fun normalName(): String? { // loses case, used in tree building for working out where in tree it should go
            return normalName
        }

        fun name(name: String?): Tag {
            tagName = name
            normalName = lowerCase(name)
            return this
        }

        // these appenders are rarely hit in not null state-- caused by null chars.
        fun appendTagName(append: String) {
            tagName = if (tagName == null) append else (tagName + append)
            normalName = lowerCase(tagName)
        }

        fun appendTagName(append: Char) {
            appendTagName(append.toString())
        }

        fun appendAttributeName(append: String) {
            pendingAttributeName =
                if (pendingAttributeName == null) append else (pendingAttributeName + append)
        }

        fun appendAttributeName(append: Char) {
            appendAttributeName(append.toString())
        }

        fun appendAttributeValue(append: String?) {
            ensureAttributeValue()
            if (pendingAttributeValue.isEmpty()) {
                pendingAttributeValueS = append
            } else {
                pendingAttributeValue.append(append)
            }
        }

        fun appendAttributeValue(append: Char) {
            ensureAttributeValue()
            pendingAttributeValue.append(append)
        }

        fun appendAttributeValue(append: CharArray?) {
            ensureAttributeValue()
            pendingAttributeValue.append(append)
        }

        fun appendAttributeValue(appendCodepoints: IntArray) {
            ensureAttributeValue()
            for (codepoint in appendCodepoints) {
                pendingAttributeValue.appendCodePoint(codepoint)
            }
        }

        fun setEmptyAttributeValue() {
            hasEmptyAttributeValue = true
        }

        private fun ensureAttributeValue() {
            hasPendingAttributeValue = true
            // if on second hit, we'll need to move to the builder
            if (pendingAttributeValueS != null) {
                pendingAttributeValue.append(pendingAttributeValueS)
                pendingAttributeValueS = null
            }
        }
    }

    class StartTag : Tag(TokenType.StartTag) {
        init {
            attributes = Attributes()
        }

        override fun reset(): Tag {
            super.reset()
            attributes = Attributes()
            // todo - would prefer these to be null, but need to check Element assertions
            return this
        }

        fun nameAttr(name: String?, attributes: Attributes?): StartTag {
            this.tagName = name
            this.attributes = attributes
            normalName = lowerCase(tagName)
            return this
        }

        override fun toString(): String {
            return if (attributes != null && attributes!!.size() > 0) "<" + name() + " " + attributes.toString() + ">"
            else "<" + name() + ">"
        }
    }

    class EndTag internal constructor() : Tag(TokenType.EndTag) {
        override fun toString(): String {
            return "</" + name() + ">"
        }
    }

    class Comment internal constructor() : Token(TokenType.Comment) {
        val data: StringBuilder = StringBuilder()
        var bogus: Boolean = false

        override fun reset(): Token {
            reset(data)
            bogus = false
            return this
        }

        fun getData(): String {
            return data.toString()
        }

        override fun toString(): String {
            return "<!--" + getData() + "-->"
        }
    }

    open class Character internal constructor() : Token(TokenType.Character) {
        private var data: String? = null

        override fun reset(): Token {
            data = null
            return this
        }

        fun data(data: String?): Character {
            this.data = data
            return this
        }

        fun getData(): String {
            return data!!
        }

        override fun toString(): String {
            return getData()
        }
    }

    class CData internal constructor(data: String?) : Character() {
        init {
            this.data(data)
        }

        override fun toString(): String {
            return "<![CDATA[" + getData() + "]]>"
        }
    }

    class EOF internal constructor() : Token(TokenType.EOF) {
        override fun reset(): Token {
            return this
        }
    }

    //    final boolean isDoctype() {
    //        return type == TokenType.Doctype;
    //    }
    //
    //    final Doctype asDoctype() {
    //        return (Doctype) this;
    //    }
    //
    //    final boolean isStartTag() {
    //        return type == TokenType.StartTag;
    //    }
    //
    //    final StartTag asStartTag() {
    //        return (StartTag) this;
    //    }
    //
    //    final boolean isEndTag() {
    //        return type == TokenType.EndTag;
    //    }
    //
    //    final EndTag asEndTag() {
    //        return (EndTag) this;
    //    }
    //
    //    final boolean isComment() {
    //        return type == TokenType.Comment;
    //    }
    //
    //    final Comment asComment() {
    //        return (Comment) this;
    //    }
    //
    //    final boolean isCharacter() {
    //        return type == TokenType.Character;
    //    }
    //
    //    final boolean isCData() {
    //        return this instanceof CData;
    //    }
    //
    //    final Character asCharacter() {
    //        return (Character) this;
    //    }
    //
    //    final boolean isEOF() {
    //        return type == TokenType.EOF;
    //    }
    enum class TokenType {
        Doctype,
        StartTag,
        EndTag,
        Comment,
        Character,  // note no CData - treated in builder as an extension of Character
        EOF
    }

    companion object {
        @JvmStatic
        fun reset(sb: StringBuilder?) {
            sb?.delete(0, sb.length)
        }
    }
}
