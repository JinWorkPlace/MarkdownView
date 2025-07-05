package com.apps.markdown.sample.samples.image

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.ImagesPlugin.ImagesConfigure
import io.noties.markwon.image.gif.GifMediaDecoder

@MarkwonSampleInfo(
    id = "20200630162214",
    title = "GIF image",
    artifacts = [MarkwonArtifact.IMAGE],
    tags = [Tag.IMAGE, Tag.GIF]
)
class GifImageSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "![gif-image](https://upload.wikimedia.org/wikipedia/commons/2/2c/Rotating_earth_%28large%29.gif)"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(ImagesPlugin.create(object : ImagesConfigure {
                override fun configureImages(plugin: ImagesPlugin) {
                    plugin.addMediaDecoder(GifMediaDecoder.create())
                }
            })).build()

        markwon.setMarkdown(textView, md)
    }
}
