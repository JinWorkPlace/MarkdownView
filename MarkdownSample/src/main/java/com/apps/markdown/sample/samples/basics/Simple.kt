package com.apps.markdown.sample.samples.basics

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon

@MarkwonSampleInfo(
    id = "20200626152255",
    title = "Simple",
    description = "The most primitive and simple way to apply markdown to a `TextView`",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.BASIC]
)
class Simple : MarkwonTextViewSample() {
    override fun render() {
        // markdown input
        val md = """
      # Heading
      
      > A quote
      
      **bold _italic_ bold**
    """.trimIndent()

        // markwon instance
        val markwon = Markwon.create(context)

        // apply raw markdown (internally parsed and rendered)
        markwon.setMarkdown(textView, md)
    }
}