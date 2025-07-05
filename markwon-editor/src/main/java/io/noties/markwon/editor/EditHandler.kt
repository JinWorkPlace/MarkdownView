package io.noties.markwon.editor

import android.text.Editable
import io.noties.markwon.Markwon

/**
 * @see io.noties.markwon.editor.handler.EmphasisEditHandler
 *
 * @see io.noties.markwon.editor.handler.StrongEmphasisEditHandler
 *
 * @since 4.2.0
 */
interface EditHandler<T> {
    fun init(markwon: Markwon)

    fun configurePersistedSpans(builder: PersistedSpans.Builder)

    // span is present only in off-screen rendered markdown, it must be processed and
    //  a NEW one must be added to editable (via edit-persist-spans)
    //
    // NB, editable.setSpan must obtain span from `spans` and must be configured beforehand
    // multiple spans are OK as long as they are configured
    /**
     * @param persistedSpans
     * @param editable
     * @param input
     * @param span
     * @param spanStart
     * @param spanTextLength
     * @see MarkwonEditorUtils
     */
    fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: T,
        spanStart: Int,
        spanTextLength: Int
    )

    fun markdownSpanType(): Class<T>
}
