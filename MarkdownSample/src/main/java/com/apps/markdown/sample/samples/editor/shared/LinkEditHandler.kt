package com.apps.markdown.sample.samples.editor.shared

import android.text.Editable
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import io.noties.markwon.core.spans.LinkSpan
import io.noties.markwon.editor.AbstractEditHandler
import io.noties.markwon.editor.PersistedSpans

class LinkEditHandler(
    private val onClick: OnClick
) : AbstractEditHandler<LinkSpan>() {
    fun interface OnClick {
        fun onClick(widget: View, link: String)
    }

    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder.persistSpan<EditLinkSpan>(
            EditLinkSpan::class.java
        ) { EditLinkSpan(onClick) }
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: LinkSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        val editLinkSpan: EditLinkSpan = persistedSpans.get(EditLinkSpan::class.java)
        editLinkSpan.link = span.link

        // First first __letter__ to find link content (scheme start in URL, receiver in email address)
        // NB! do not use phone number auto-link (via LinkifyPlugin) as we cannot guarantee proper link
        //  display. For example, we _could_ also look for a digit, but:
        //  * if phone number start with special symbol, we won't have it (`+`, `(`)
        //  * it might interfere with an ordered-list
        var start = -1

        var i = spanStart
        val length = input.length
        while (i < length) {
            if (Character.isLetter(input[i])) {
                start = i
                break
            }
            i++
        }

        if (start > -1) {
            editable.setSpan(
                editLinkSpan, start, start + spanTextLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun markdownSpanType(): Class<LinkSpan> {
        return LinkSpan::class.java
    }

    internal class EditLinkSpan(private val onClick: OnClick) : ClickableSpan() {
        var link: String? = null

        override fun onClick(widget: View) {
            if (link != null) {
                onClick.onClick(widget, link!!)
            }
        }
    }
}
