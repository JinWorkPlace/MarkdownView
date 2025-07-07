package io.noties.markwon.editor

import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import io.noties.markwon.Markwon
import io.noties.markwon.editor.diff_match_patch.diff_main

internal class MarkwonEditorImpl<T>(
    private val markwon: Markwon,
    private val persistedSpansProvider: PersistedSpans.Provider,
    private val punctuationSpanType: Class<*>,
    private val spansHandler: SpansHandler<T>?
) : MarkwonEditor() {
    override fun process(editable: Editable) {
        val input = editable.toString()

        // NB, we cast to Spannable here without prior checks
        //  if by some occasion Markwon stops returning here a Spannable our tests will catch that
        //  (we need Spannable in order to remove processed spans, so they do not appear multiple times)
        val renderedMarkdown = markwon.toMarkdown(input) as Spannable

        val markdown = renderedMarkdown.toString()

        val spansHandler = this.spansHandler
        val hasAdditionalSpans = spansHandler != null

        val persistedSpans = persistedSpansProvider.provide(editable)
        try {
            val diffs: MutableList<diff_match_patch.Diff> = diff_main(input, markdown)

            var inputLength = 0
            var markdownLength = 0

            for (diff in diffs) {
                when (diff.operation) {
                    diff_match_patch.Operation.DELETE -> {
                        val start = inputLength
                        inputLength += diff.text!!.length

                        editable.setSpan(
                            persistedSpans.get(punctuationSpanType),
                            start,
                            inputLength,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        if (hasAdditionalSpans) {
                            // obtain spans for a single character of renderedMarkdown
                            //  editable here should return all spans that are contained in specified
                            //  region. Later we match if span starts at current position
                            //  and notify additional span handler about it
                            val spans = renderedMarkdown.getSpans(
                                markdownLength, markdownLength + 1, Any::class.java
                            )
                            for (span in spans) {
                                if (markdownLength == renderedMarkdown.getSpanStart(span)) {
                                    span?.let {
                                        spansHandler.handle(
                                            persistedSpans,
                                            editable,
                                            input,
                                            span as T,
                                            start,
                                            renderedMarkdown.getSpanEnd(span) - markdownLength
                                        )
                                    }

                                    // NB, we do not break here in case of SpanFactory
                                    // returns multiple spans for a markdown node, this way
                                    // we will handle all of them

                                    // It is important to remove span after we have processed it
                                    //  as we process them in 2 places: here and in EQUAL
                                    renderedMarkdown.removeSpan(span)
                                }
                            }
                        }
                    }

                    diff_match_patch.Operation.INSERT ->                         // no special handling here, but still we must advance the markdownLength
                        markdownLength += diff.text!!.length

                    diff_match_patch.Operation.EQUAL -> {
                        val length = diff.text!!.length
                        val inputStart = inputLength
                        val markdownStart = markdownLength
                        inputLength += length
                        markdownLength += length

                        // it is possible that there are spans for the text that is the same
                        //  for example, if some links were _autolinked_ (text is the same,
                        //  but there is an additional URLSpan)
                        if (hasAdditionalSpans) {
                            val spans = renderedMarkdown.getSpans(
                                markdownStart, markdownLength, Any::class.java
                            )
                            for (span in spans) {
                                val spanStart = renderedMarkdown.getSpanStart(span)
                                if (spanStart >= markdownStart) {
                                    val end = renderedMarkdown.getSpanEnd(span)
                                    if (end <= markdownLength) {
                                        span?.let {
                                            spansHandler.handle(
                                                persistedSpans,
                                                editable,
                                                input,
                                                span as T,  // shift span to input position (can be different from the text itself)
                                                inputStart + (spanStart - markdownStart),
                                                end - spanStart
                                            )
                                        }
                                        renderedMarkdown.removeSpan(span)
                                    }
                                }
                            }
                        }
                    }

                    else -> throw IllegalStateException()
                }
            }
        } finally {
            persistedSpans.removeUnused()
        }
    }

    override fun preRender(editable: Editable, preRenderListener: PreRenderResultListener) {
        val builder = RecordingSpannableStringBuilder(editable)
        process(builder)
        preRenderListener.onPreRenderResult(object : PreRenderResult {
            override fun resultEditable(): Editable {
                // if they are the same, they should be equals then (what about additional spans?? like cursor? it should not interfere....)
                return builder
            }

            override fun dispatchTo(editable: Editable) {
                for (span in builder.applied) {
                    editable.setSpan(span.what, span.start, span.end, span.flags)
                }
                for (span in builder.removed) {
                    editable.removeSpan(span)
                }
            }
        })
    }

    private class Span(val what: Any?, val start: Int, val end: Int, val flags: Int)

    private class RecordingSpannableStringBuilder(text: CharSequence?) :
        SpannableStringBuilder(text) {
        val applied: MutableList<Span> = ArrayList(3)
        val removed: MutableList<Any?> = ArrayList(0)

        override fun setSpan(what: Any?, start: Int, end: Int, flags: Int) {
            super.setSpan(what, start, end, flags)
            applied.add(Span(what, start, end, flags))
        }

        override fun removeSpan(what: Any?) {
            super.removeSpan(what)
            removed.add(what)
        }
    }
}
