package io.noties.markwon.html

import android.text.TextUtils

abstract class CssInlineStyleParser {
    abstract fun parse(inlineStyle: String): Iterable<CssProperty>

    internal class Impl : CssInlineStyleParser() {
        override fun parse(inlineStyle: String): Iterable<CssProperty> {
            return CssIterable(inlineStyle)
        }

        private class CssIterable(val input: String) : Iterable<CssProperty> {
            override fun iterator(): MutableIterator<CssProperty> {
                return CssIterator()
            }

            private inner class CssIterator : MutableIterator<CssProperty> {
                private val cssProperty = CssProperty()

                private val builder = StringBuilder()

                private val length = input.length

                private var index = 0

                override fun hasNext(): Boolean {
                    prepareNext()

                    return hasNextPrepared()
                }

                override fun next(): CssProperty {
                    if (!hasNextPrepared()) {
                        throw NoSuchElementException()
                    }
                    return cssProperty
                }

                fun prepareNext() {
                    // clear first

                    cssProperty.set("", "")

                    builder.setLength(0)

                    var key: String? = null
                    var value: String? = null

                    var c: Char

                    var keyHasWhiteSpace = false

                    for (i in index..<length) {
                        c = input[i]

                        // if we are building KEY, then when we encounter WS (white-space) we finish
                        // KEY and wait for the ':', if we do not find it and we find EOF or ';'
                        // we start creating KEY again after the ';'
                        if (key == null) {
                            if (':' == c) {
                                // we have no key yet, but we might have started creating it already

                                if (builder.isNotEmpty()) {
                                    key = builder.toString().trim { it <= ' ' }
                                }

                                builder.setLength(0)
                            } else {
                                // if by any chance we have here the ';' -> reset key and try to match next
                                if (';' == c) {
                                    builder.setLength(0)
                                } else {
                                    // key cannot have WS gaps (but leading and trailing are OK)

                                    if (Character.isWhitespace(c)) {
                                        if (builder.isNotEmpty()) {
                                            keyHasWhiteSpace = true
                                        }
                                    } else {
                                        // if not a WS and we have found WS before, start a-new
                                        // else append
                                        if (keyHasWhiteSpace) {
                                            // start new filling
                                            builder.setLength(0)
                                            builder.append(c)
                                            // clear this flag
                                            keyHasWhiteSpace = false
                                        } else {
                                            builder.append(c)
                                        }
                                    }
                                }
                            }
                        } else if (value == null) {
                            if (Character.isWhitespace(c)) {
                                if (builder.isNotEmpty()) {
                                    builder.append(c)
                                }
                            } else if (';' == c) {
                                value = builder.toString().trim { it <= ' ' }
                                builder.setLength(0)

                                // check if we have valid values -> if yes -> return it
                                if (hasValues(key, value)) {
                                    index = i + 1
                                    cssProperty.set(key, value)
                                    return
                                }
                            } else {
                                builder.append(c)
                            }
                        }
                    }

                    // here we must additionally check for EOF (we might be tracking value here)
                    if (key != null && builder.isNotEmpty()) {
                        value = builder.toString().trim { it <= ' ' }
                        cssProperty.set(key, value)
                        index = length
                    }
                }

                fun hasNextPrepared(): Boolean {
                    return hasValues(cssProperty.key(), cssProperty.value())
                }

                fun hasValues(key: String?, value: String?): Boolean {
                    return !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)
                }

                override fun remove() {

                }
            }

        }
    }

    companion object {
        @JvmStatic
        fun create(): CssInlineStyleParser {
            return Impl()
        }
    }
}
