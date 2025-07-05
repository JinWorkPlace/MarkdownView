package io.noties.markwon.editor.handler

import android.text.Editable
import android.text.Spanned
import io.noties.markwon.core.spans.StrongEmphasisSpan
import io.noties.markwon.editor.AbstractEditHandler
import io.noties.markwon.editor.MarkwonEditorUtils
import io.noties.markwon.editor.PersistedSpans

/**
 * @since 4.2.0
 */
class StrongEmphasisEditHandler : AbstractEditHandler<StrongEmphasisSpan>() {
    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder.persistSpan(
            StrongEmphasisSpan::class.java,
            object : PersistedSpans.SpanFactory<StrongEmphasisSpan> {
                override fun create(): StrongEmphasisSpan {
                    return StrongEmphasisSpan()
                }
            }
        )
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: StrongEmphasisSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        // inline spans can delimit other inline spans,
        //  for example: `**_~~hey~~_**`, this is why we must additionally find delimiter used
        //  and its actual start/end positions
        val match =
            MarkwonEditorUtils.findDelimited(input, spanStart, "**", "__")
        if (match != null) {
            editable.setSpan(
                persistedSpans.get(StrongEmphasisSpan::class.java),
                match.start(),
                match.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun markdownSpanType(): Class<StrongEmphasisSpan> {
        return StrongEmphasisSpan::class.java
    }

    companion object {
        fun create(): StrongEmphasisEditHandler {
            return StrongEmphasisEditHandler()
        }
    }
}
