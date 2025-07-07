package com.apps.markdown.sample.samples.editor

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.samples.editor.shared.HeadingEditHandler
import com.apps.markdown.sample.samples.editor.shared.MarkwonEditTextSample
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher

@MarkwonSampleInfo(
    id = "20200630113954",
    title = "Heading edit handler",
    description = "Handling of heading node in editor",
    artifacts = [MarkwonArtifact.EDITOR, MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.EDITOR]
)
class EditorHeadingSample : MarkwonEditTextSample() {
    override fun render() {
        val markwon: Markwon = Markwon.create(context)
        val editor: MarkwonEditor =
            MarkwonEditor.builder(markwon).useEditHandler(HeadingEditHandler()).build()

        editText.addTextChangedListener(
            MarkwonEditorTextWatcher.withPreRender(
                editor, java.util.concurrent.Executors.newSingleThreadExecutor(), editText
            )
        )
    }
}
