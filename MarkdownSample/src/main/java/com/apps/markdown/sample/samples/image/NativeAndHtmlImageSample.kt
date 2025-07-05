package com.apps.markdown.sample.samples.image

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.AsyncDrawableSpan
import io.noties.markwon.image.ImageProps
import io.noties.markwon.image.ImageSize
import io.noties.markwon.image.ImagesPlugin

@MarkwonSampleInfo(
    id = "20200803115847",
    title = "Native and HTML image",
    description = "Define images in both native markdown and HTML. Native markdown images take 100% of available width",
    artifacts = [MarkwonArtifact.IMAGE, MarkwonArtifact.HTML],
    tags = [Tag.RENDERING, Tag.IMAGE, Tag.HTML]
)
class NativeAndHtmlImageSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Native image\n" + "![alt](https://picsum.photos/id/237/1024/800)\n\n" + "# HTML image\n" + "<img src=\"https://picsum.photos/id/237/1024/800\" width=\"100%\" height=\"auto\"></img>" + ""

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(ImagesPlugin.create()).usePlugin(HtmlPlugin.create())
                .usePlugin(object : AbstractMarkwonPlugin() {
                    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                        builder.setFactory(
                            org.commonmark.node.Image::class.java, object : SpanFactory {
                                override fun getSpans(
                                    configuration: MarkwonConfiguration, props: RenderProps
                                ): Any {
                                    return AsyncDrawableSpan(
                                        configuration.theme(),
                                        AsyncDrawable(
                                            ImageProps.DESTINATION.require(props),
                                            configuration.asyncDrawableLoader(),
                                            configuration.imageSizeResolver(),
                                            imageSize(props)
                                        ),
                                        AsyncDrawableSpan.ALIGN_BOTTOM,
                                        ImageProps.REPLACEMENT_TEXT_IS_LINK.get(props, false)
                                    )
                                }
                            })
                    }
                }).build()

        markwon.setMarkdown(textView, md)
    }

    companion object {
        // Use defined image size or make its width 100%
        private fun imageSize(props: RenderProps): ImageSize {
            val imageSize: ImageSize? = ImageProps.IMAGE_SIZE.get(props)
            if (imageSize != null) {
                return imageSize
            }
            return ImageSize(
                ImageSize.Dimension(100F, "%"), ImageSize.Dimension(0F, "%")
            )
        }
    }
}
