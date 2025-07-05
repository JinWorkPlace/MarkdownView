package com.apps.markdown.sample.samples.editor

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.samples.editor.shared.MarkwonEditTextSample
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher

@MarkwonSampleInfo(
    id = "20200629164422",
    title = "Editor with pre-render (async)",
    description = "Editor functionality with highlight " + "taking place in another thread",
    artifacts = [MarkwonArtifact.EDITOR, MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.EDITOR]
)
class EditorPreRenderSample : MarkwonEditTextSample() {
    override fun render() {
        // Process highlight in background thread

        val markwon: Markwon = Markwon.create(context)
        val editor: MarkwonEditor = MarkwonEditor.create(markwon)

        editText.addTextChangedListener(
            MarkwonEditorTextWatcher.withPreRender(
                editor, java.util.concurrent.Executors.newCachedThreadPool(), editText
            )
        )
    }
}
