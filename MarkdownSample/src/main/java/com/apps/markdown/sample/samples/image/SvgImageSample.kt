package com.apps.markdown.sample.samples.image

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.svg.SvgPictureMediaDecoder

@MarkwonSampleInfo(
    id = "20200630161952",
    title = "SVG image",
    artifacts = [MarkwonArtifact.IMAGE],
    tags = [Tag.IMAGE, Tag.SVG]
)
class SvgImageSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "![svg-image](https://github.com/dcurtis/markdown-mark/raw/master/svg/markdown-mark-solid.svg)"

        val markwon: Markwon =
            Markwon.builder(context) // SVG and GIF are automatically handled if required
                //  libraries are in path (specified in dependencies block)
                //      .usePlugin(ImagesPlugin.create())
                // let's make it implicit
                .usePlugin(ImagesPlugin.create(object : ImagesPlugin.ImagesConfigure {
                    override fun configureImages(plugin: ImagesPlugin) {
                        // there 2 svg media decoders:
                        // - regular `SvgMediaDecoder`
                        // - special one when SVG doesn't have width and height specified - `SvgPictureMediaDecoder`
                        plugin.addMediaDecoder(SvgPictureMediaDecoder.create())
                    }
                })).build()

        markwon.setMarkdown(textView, md)
    }
}
