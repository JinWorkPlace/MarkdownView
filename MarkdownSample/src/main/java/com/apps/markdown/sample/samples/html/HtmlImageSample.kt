package com.apps.markdown.sample.samples.html

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin

@MarkwonSampleInfo(
    id = "20200630115300",
    title = "Html images",
    description = "Usage of HTML images",
    artifacts = [MarkwonArtifact.HTML, MarkwonArtifact.IMAGE],
    tags = [Tag.IMAGE, Tag.RENDERING, Tag.HTML]
)
class HtmlImageSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "## Try CommonMark\n" + "\n" + "Markwon IMG:\n" + "\n" + "![](https://upload.wikimedia.org/wikipedia/it/thumb/c/c5/GTA_2.JPG/220px-GTA_2.JPG)\n" + "\n" + "New lines...\n" + "\n" + "HTML IMG:\n" + "\n" + "<img src=\"https://upload.wikimedia.org/wikipedia/it/thumb/c/c5/GTA_2.JPG/220px-GTA_2.JPG\"></img>\n" + "\n" + "New lines\n\n"

        Markwon.builder(context).usePlugin(ImagesPlugin.create()).usePlugin(HtmlPlugin.create())
            .build().setMarkdown(textView, md)
    }
}
