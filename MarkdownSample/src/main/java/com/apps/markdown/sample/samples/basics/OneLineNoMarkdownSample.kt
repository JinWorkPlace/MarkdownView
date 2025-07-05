package com.apps.markdown.sample.samples.basics

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon

@MarkwonSampleInfo(
    id = "20210118154116",
    title = "One line text",
    description = "Single line text without markdown markup",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.RENDERING]
)
class OneLineNoMarkdownSample : MarkwonTextViewSample() {
    override fun render() {
        textView.setBackgroundColor(0x40ff0000)

        val md = " Demo text "

        val markwon: Markwon = Markwon.builder(context).build()

        markwon.setMarkdown(textView, md)
    }
}
