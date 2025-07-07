package com.apps.markdown.sample.samples.editor.shared

import android.text.Editable
import android.text.Spanned
import android.text.style.StrikethroughSpan
import io.noties.markwon.editor.AbstractEditHandler
import io.noties.markwon.editor.MarkwonEditorUtils.findDelimited
import io.noties.markwon.editor.PersistedSpans

class StrikethroughEditHandler : AbstractEditHandler<StrikethroughSpan>() {
    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder.persistSpan(
            StrikethroughSpan::class.java
        ) { StrikethroughSpan() }
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: StrikethroughSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        val match = findDelimited(input, spanStart, "~~")
        if (match != null) {
            editable.setSpan(
                persistedSpans.get(StrikethroughSpan::class.java),
                match.start(),
                match.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun markdownSpanType(): Class<StrikethroughSpan> {
        return StrikethroughSpan::class.java
    }
}
