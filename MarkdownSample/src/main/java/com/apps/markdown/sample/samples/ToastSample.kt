package com.apps.markdown.sample.samples

import android.widget.Toast
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon

@MarkwonSampleInfo(
    id = "20200627072642",
    title = "Markdown in Toast",
    description = "Display _static_ markdown content in a `android.widget.Toast`",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.TOAST]
)
class ToastSample : MarkwonTextViewSample() {
    override fun render() {
        // NB! only _static_ content is going to be displayed,
        //  so, no images, tables or latex in a Toast
        val md = """
      # Heading is fine
      > Even quote if **fine**
      ```
      finally code works;
      ```
      _italic_ to put an end to it
    """.trimIndent()

        val markwon = Markwon.create(context)

        // render raw input to styled markdown
        val markdown = markwon.toMarkdown(md)

        // Toast accepts CharSequence and allows styling via spans
        Toast.makeText(context, markdown, Toast.LENGTH_LONG).show()
    }
}