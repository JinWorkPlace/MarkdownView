package com.apps.markdown.sample.samples.editor

import android.text.Editable
import android.text.Spanned
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.samples.editor.shared.MarkwonEditTextSample
import io.noties.markwon.Markwon
import io.noties.markwon.core.spans.StrongEmphasisSpan
import io.noties.markwon.editor.AbstractEditHandler
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import io.noties.markwon.editor.MarkwonEditorUtils
import io.noties.markwon.editor.PersistedSpans

@MarkwonSampleInfo(
    id = "20200629165136",
    title = "Additional edit span",
    description = "Additional _edit_ span (span that is present in " + "`EditText` along with punctuation",
    artifacts = [MarkwonArtifact.EDITOR, MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.EDITOR, Tag.SPAN]
)
class EditorAdditionalEditSpan : MarkwonEditTextSample() {
    public override fun render() {
        // An additional span is used to highlight strong-emphasis

        val editor: MarkwonEditor =
            MarkwonEditor.builder(Markwon.create(context)).useEditHandler(BoldEditHandler()).build()

        editText.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor))
    }
}

internal class BoldEditHandler : AbstractEditHandler<StrongEmphasisSpan>() {
    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        // Here we define which span is _persisted_ in EditText, it is not removed
        //  from EditText between text changes, but instead - reused (by changing
        //  position). Consider it as a cache for spans. We could use `StrongEmphasisSpan`
        //  here also, but I chose Bold to indicate that this span is not the same
        //  as in off-screen rendered markdown
        builder.persistSpan(
            Bold::class.java
        ) { Bold() }
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: StrongEmphasisSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        // Unfortunately we cannot hardcode delimiters length here (aka spanTextLength + 4)
        //  because multiple inline markdown nodes can refer to the same text.
        //  For example, `**_~~hey~~_**` - we will receive `**_~~` in this method,
        //  and thus will have to manually find actual position in raw user input
        val match: MarkwonEditorUtils.Match? =
            MarkwonEditorUtils.findDelimited(input, spanStart, "**", "__")
        if (match != null) {
            editable.setSpan( // we handle StrongEmphasisSpan and represent it with Bold in EditText
                //  we still could use StrongEmphasisSpan, but it must be accessed
                //  via persistedSpans
                persistedSpans.get(Bold::class.java),
                match.start(),
                match.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun markdownSpanType(): Class<StrongEmphasisSpan> {
        return StrongEmphasisSpan::class.java
    }
}

internal class Bold : MetricAffectingSpan() {
    override fun updateDrawState(tp: TextPaint) {
        update(tp)
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        update(textPaint)
    }

    private fun update(paint: TextPaint) {
        paint.isFakeBoldText = true
    }
}
