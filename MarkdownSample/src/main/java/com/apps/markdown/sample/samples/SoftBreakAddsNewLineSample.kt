package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin

@MarkwonSampleInfo(
    id = "20200629125040",
    title = "Soft break new line",
    description = "Add a new line for a markdown soft-break node",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.NEW_LINE, Tag.SOFT_BREAK]
)
class SoftBreakAddsNewLineSample : MarkwonTextViewSample() {
    override fun render() {
        val md = "" + "Hello there ->(line)\n(break)<- going on and on"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(SoftBreakAddsNewLinePlugin.create()).build()

        markwon.setMarkdown(textView, md)
    }
}
