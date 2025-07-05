package com.apps.markdown.sample.samples.image

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.image.glide.GlideImagesPlugin

@MarkwonSampleInfo(
    id = "20200630170112",
    title = "Glide image",
    artifacts = [MarkwonArtifact.IMAGE_GLIDE],
    tags = [Tag.IMAGE]
)
class GlideImageSample : MarkwonTextViewSample() {
    public override fun render() {
        val md =
            "[![undefined](https://img.youtube.com/vi/gs1I8_m4AOM/0.jpg)](https://www.youtube.com/watch?v=gs1I8_m4AOM)"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(GlideImagesPlugin.create(context)).build()

        markwon.setMarkdown(textView, md)
    }
}
