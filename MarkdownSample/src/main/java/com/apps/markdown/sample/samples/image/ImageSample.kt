package com.apps.markdown.sample.samples.image

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.image.ImagesPlugin

@MarkwonSampleInfo(
    id = "20200630144659",
    title = "Markdown image",
    artifacts = [MarkwonArtifact.IMAGE],
    tags = [Tag.IMAGE]
)
class ImageSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "![image](https://github.com/dcurtis/markdown-mark/raw/master/png/208x128-solid.png)"

        val markwon: Markwon = Markwon.builder(context).usePlugin(ImagesPlugin.create()).build()

        markwon.setMarkdown(textView, md)
    }
}
