package com.apps.markdown.sample.samples.editor

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.samples.editor.shared.MarkwonEditTextSample
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher

@MarkwonSampleInfo(
    id = "20200629164227",
    title = "Simple editor",
    description = "Simple usage of editor with markdown highlight",
    artifacts = [MarkwonArtifact.EDITOR, MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.EDITOR]
)
class EditorSimpleSample : MarkwonEditTextSample() {
    override fun render() {
        // Process highlight in-place (right after text has changed)

        // obtain Markwon instance

        val markwon: Markwon = Markwon.create(context)

        // create editor
        val editor: MarkwonEditor = MarkwonEditor.create(markwon)

        // set edit listener
        editText.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor))
    }
}
