package io.noties.markwon.editor

import android.text.Spannable
import android.util.Log
import java.util.Locale

/**
 * Cache for spans that present in user input. These spans are reused between different
 * [MarkwonEditor.process] and [MarkwonEditor.preRender]
 * calls.
 *
 * @see EditHandler.handleMarkdownSpan
 * @see EditHandler.configurePersistedSpans
 * @since 4.2.0
 */
abstract class PersistedSpans {
    interface SpanFactory<T> {
        fun create(): T
    }

    interface Builder {
        fun <T> persistSpan(type: Class<T>, spanFactory: SpanFactory<T>): Builder
    }

    abstract fun <T> get(type: Class<T>): T

    abstract fun removeUnused()


    class Provider : Builder {
        private val map: MutableMap<Class<*>, SpanFactory<*>> =
            HashMap(3)

        override fun <T> persistSpan(type: Class<T>, spanFactory: SpanFactory<T>): Builder {
            if (map.put(type, spanFactory) != null) {
                Log.e(
                    "MD-EDITOR", String.format(
                        Locale.ROOT,
                        "Re-declaration of persisted span for '%s'", type.name
                    )
                )
            }
            return this
        }

        fun provide(spannable: Spannable): PersistedSpans {
            return Impl(spannable, map)
        }
    }

    internal class Impl(
        private val spannable: Spannable,
        private val spans: MutableMap<Class<*>, SpanFactory<*>>
    ) : PersistedSpans() {
        private val map: MutableMap<Class<*>, MutableList<Any?>> =
            MarkwonEditorUtils.extractSpans(spannable, spans.keys)

        override fun <T> get(type: Class<T>): T {
            val span: Any?

            val list = map[type]
            if (list != null && list.isNotEmpty()) {
                span = list.removeAt(0)
            } else {
                val spanFactory = spans[type]
                checkNotNull(spanFactory) {
                    "Requested type `" + type.name + "` was " +
                            "not registered, use PersistedSpans.Builder#persistSpan method to register"
                }
                span = spanFactory.create()
            }

            return span as T
        }

        override fun removeUnused() {
            for (spans in map.values) {
                if (spans.isNotEmpty()) {
                    for (span in spans) {
                        spannable.removeSpan(span)
                    }
                }
            }
        }
    }

    companion object {
        fun provider(): Provider {
            return Provider()
        }
    }
}
