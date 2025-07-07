package io.noties.markwon.editor

import android.text.Editable
import com.google.gson.reflect.TypeToken
import io.noties.markwon.Markwon

/**
 * @see .builder
 * @see .create
 * @see .process
 * @see .preRender
 * @since 4.2.0
 */
abstract class MarkwonEditor {
    /**
     * @see .preRender
     */
    interface PreRenderResult {
        /**
         * @return Editable instance for which result was calculated. This must not be
         * actual Editable of EditText
         */
        fun resultEditable(): Editable

        /**
         * Dispatch pre-rendering result to EditText
         *
         * @param editable to dispatch result to
         */
        fun dispatchTo(editable: Editable)
    }

    /**
     * @see .preRender
     */
    interface PreRenderResultListener {
        fun onPreRenderResult(result: PreRenderResult)
    }

    /**
     * Synchronous method that processes supplied Editable in-place. If you wish to move this job
     * to another thread consider using [.preRender]
     *
     * @param editable to process
     * @see .preRender
     */
    abstract fun process(editable: Editable)

    /**
     * Pre-render highlight result. Can be useful to create highlight information on a different
     * thread.
     *
     *
     * Please note that currently only `setSpan` and `removeSpan` actions will be recorded (and thus dispatched).
     * Make sure you use only these methods in your [EditHandler], or implement the required
     * functionality some other way.
     *
     * @param editable          to process and pre-render
     * @param preRenderListener listener to be notified when pre-render result will be ready
     * @see .process
     */
    abstract fun preRender(editable: Editable, preRenderListener: PreRenderResultListener)


    class Builder internal constructor(private val markwon: Markwon) {
        private val persistedSpansProvider = PersistedSpans.provider()
        private val editHandlers: MutableMap<Class<*>, EditHandler<*>> = HashMap(0)

        private var punctuationSpanType: Class<*>? = null

        fun <T> useEditHandler(handler: EditHandler<T>): Builder {
            this.editHandlers.put(handler.markdownSpanType(), handler)
            return this
        }


        /**
         * Specify which punctuation span will be used.
         *
         * @param type    of the span
         * @param factory to create a new instance of the span
         */
        fun <T> punctuationSpan(type: Class<T>, factory: PersistedSpans.SpanFactory<T>): Builder {
            this.punctuationSpanType = type
            this.persistedSpansProvider.persistSpan(type, factory)
            return this
        }

        fun build(): MarkwonEditor {
            var punctuationSpanType = this.punctuationSpanType
            if (punctuationSpanType == null) {
                punctuationSpan(
                    PunctuationSpan::class.java,
                    object : PersistedSpans.SpanFactory<PunctuationSpan> {
                        override fun create(): PunctuationSpan {
                            return PunctuationSpan()
                        }
                    })
                punctuationSpanType = this.punctuationSpanType
            }

            for (handler in editHandlers.values) {
                handler.init(markwon)
                handler.configurePersistedSpans(persistedSpansProvider)
            }

            val spansHandler: SpansHandler<*>? = if (editHandlers.isEmpty()) null
            else SpansHandlerImpl(editHandlers as MutableMap<Class<Any>, EditHandler<Any>>)

            return MarkwonEditorImpl(
                markwon, persistedSpansProvider, punctuationSpanType!!, spansHandler
            )
        }
    }

    internal interface SpansHandler<T> {
        fun handle(
            spans: PersistedSpans,
            editable: Editable,
            input: String,
            span: T,
            spanStart: Int,
            spanTextLength: Int
        )
    }

    internal class SpansHandlerImpl<T>(
        private val spanHandlers: MutableMap<Class<T>, EditHandler<T>>
    ) : SpansHandler<T> {
        override fun handle(
            spans: PersistedSpans,
            editable: Editable,
            input: String,
            span: T,
            spanStart: Int,
            spanTextLength: Int
        ) {
            val handler: EditHandler<T>? = spanHandlers[object : TypeToken<T>() {}.type]
            handler?.handleMarkdownSpan(
                spans, editable, input, span, spanStart, spanTextLength
            )
        }
    }

    companion object {
        /**
         * Creates default instance of [MarkwonEditor]. By default it will handle only
         * punctuation spans (highlight markdown punctuation and nothing more).
         *
         * @see .builder
         */
        fun create(markwon: Markwon): MarkwonEditor {
            return builder(markwon).build()
        }

        /**
         * @see .create
         * @see Builder
         */
        fun builder(markwon: Markwon): Builder {
            return Builder(markwon)
        }
    }
}
