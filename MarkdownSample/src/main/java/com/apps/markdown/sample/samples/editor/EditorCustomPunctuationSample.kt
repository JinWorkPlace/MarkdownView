package com.apps.markdown.sample.samples.editor

import android.text.style.ForegroundColorSpan
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.samples.editor.shared.MarkwonEditTextSample
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher

@MarkwonSampleInfo(
    id = "20200629164627",
    title = "Custom punctuation span",
    description = "Custom span for punctuation in editor",
    artifacts = [MarkwonArtifact.EDITOR, MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.EDITOR, Tag.SPAN]
)
class EditorCustomPunctuationSample : MarkwonEditTextSample() {
    public override fun render() {
        // Use own punctuation span

        val editor: MarkwonEditor = MarkwonEditor.builder(Markwon.create(context))
            .punctuationSpan(CustomPunctuationSpan::class.java) { CustomPunctuationSpan() }.build()

        editText.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor))
    }
}

internal class CustomPunctuationSpan : ForegroundColorSpan(-0x10000)
