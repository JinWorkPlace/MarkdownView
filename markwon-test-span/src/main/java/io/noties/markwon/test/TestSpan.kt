package io.noties.markwon.test

import java.util.Collections

/**
 * Utility class to validate spannable content
 *
 * @since 3.0.0
 */
abstract class TestSpan internal constructor() {
    abstract fun children(): MutableList<TestSpan>

    abstract override fun hashCode(): Int

    abstract override fun equals(other: Any?): Boolean


    abstract class Document : TestSpan() {
        abstract fun wholeText(): String
    }

    abstract class Text : TestSpan() {
        abstract fun literal(): String

        abstract fun length(): Int
    }

    // important: children should not be included in equals...
    abstract class Span : TestSpan() {
        abstract fun name(): String

        abstract fun arguments(): MutableMap<String?, Any?>

        abstract override fun children(): MutableList<TestSpan>
    }

    companion object {
        @JvmStatic
        fun document(vararg children: TestSpan): Document {
            return TestSpanDocument(children(*children))
        }

        @JvmStatic
        fun span(name: String, vararg children: TestSpan): Span {
            return span(name, mutableMapOf(), *children)
        }

        fun span(
            name: String, arguments: MutableMap<String?, Any?>, vararg children: TestSpan
        ): Span {
            return TestSpanSpan(name, children(*children), arguments)
        }

        @JvmStatic
        fun text(literal: String): Text {
            return TestSpanText(literal)
        }

        fun children(vararg children: TestSpan): MutableList<TestSpan> {
            val length = children.size
            val list: MutableList<TestSpan>
            when (length) {
                0 -> {
                    list = mutableListOf()
                }

                1 -> {
                    list = mutableListOf(children[0])
                }

                else -> {
                    val spans: MutableList<TestSpan?> = ArrayList(length)
                    Collections.addAll(spans, *children)
                    list = Collections.unmodifiableList(spans)
                }
            }
            return list
        }

        @JvmStatic
        fun args(vararg args: Any?): MutableMap<String?, Any?> {
            val length = args.size
            if (length == 0) {
                return mutableMapOf()
            }

            // validate that length is even (k=v)
            check((length % 2) == 0) {
                "Supplied key-values array must contain " + "even number of arguments"
            }

            val map: MutableMap<String?, Any?> = HashMap(length / 2 + 1)

            var key: String?
            var value: Any?

            var i = 0
            while (i < length) {
                // possible class-cast exception
                key = args[i] as String?
                value = args[i + 1]
                map.put(key, value)
                i += 2
            }

            return Collections.unmodifiableMap<String?, Any?>(map)
        }
    }
}
