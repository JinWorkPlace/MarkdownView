package io.noties.markwon.editor.handler

import android.text.Editable
import android.text.Spanned
import io.noties.markwon.core.spans.EmphasisSpan
import io.noties.markwon.editor.AbstractEditHandler
import io.noties.markwon.editor.MarkwonEditorUtils
import io.noties.markwon.editor.PersistedSpans

/**
 * @since 4.2.0
 */
class EmphasisEditHandler : AbstractEditHandler<EmphasisSpan>() {
    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder.persistSpan(
            EmphasisSpan::class.java, object : PersistedSpans.SpanFactory<EmphasisSpan> {
                override fun create(): EmphasisSpan {
                    return EmphasisSpan()
                }
            })
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: EmphasisSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        val match = MarkwonEditorUtils.findDelimited(input, spanStart, "*", "_")
        match?.let {
            editable.setSpan(
                persistedSpans.get(EmphasisSpan::class.java),
                match.start(),
                match.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun markdownSpanType(): Class<EmphasisSpan> {
        return EmphasisSpan::class.java
    }
}
