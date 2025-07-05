package com.apps.markdown.sample.samples.editor

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.samples.editor.shared.MarkwonEditTextSample
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin

@MarkwonSampleInfo(
    id = "20200629165347",
    title = "Additional plugin",
    description = "Additional plugin for editor",
    artifacts = [MarkwonArtifact.EDITOR, MarkwonArtifact.INLINE_PARSER, MarkwonArtifact.EXT_STRIKETHROUGH],
    tags = [Tag.EDITOR]
)
class EditorAdditionalPluginSample : MarkwonEditTextSample() {
    public override fun render() {
        // As highlight works based on text-diff, everything that is present in input,
        // but missing in resulting markdown is considered to be punctuation, this is why
        // additional plugins do not need special handling

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(StrikethroughPlugin.create()).build()

        val editor: MarkwonEditor = MarkwonEditor.create(markwon)

        editText.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor))
    }
}
