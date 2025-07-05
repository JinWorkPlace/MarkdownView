package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon

@MarkwonSampleInfo(
    id = "20200629124706",
    title = "Soft break adds space",
    description = "By default a soft break (`\n`) will " + "add a space character instead of new line",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.NEW_LINE, Tag.SOFT_BREAK, Tag.DEFAULTS]
)
class SoftBreakAddsSpace : MarkwonTextViewSample() {
    override fun render() {
        val md = "" + "Hello there ->(line)\n(break)<- going on and on"

        // by default a soft break will add a space (instead of line break)
        val markwon: Markwon = Markwon.create(context)

        markwon.setMarkdown(textView, md)
    }
}
