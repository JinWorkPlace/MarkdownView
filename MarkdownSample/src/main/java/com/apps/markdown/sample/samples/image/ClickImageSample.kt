package com.apps.markdown.sample.samples.image

import android.view.View
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.readme.GithubImageDestinationProcessor
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.utils.loadReadMe
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.LinkResolver
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.spans.LinkSpan
import io.noties.markwon.image.ImageProps
import io.noties.markwon.image.ImagesPlugin

import org.commonmark.node.Image

@MarkwonSampleInfo(
    id = "20201221130230",
    title = "Click images",
    description = "Make _all_ images clickable (to open in a gallery, etc)",
    artifacts = [MarkwonArtifact.IMAGE],
    tags = [Tag.RENDERING, Tag.IMAGE]
)
class ClickImageSample : MarkwonTextViewSample() {
    override fun render() {

        val md = loadReadMe(context)

        // please note that if an image is already inside a link, original link would be overriden

        val markwon = Markwon.builder(context).usePlugin(ImagesPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder.imageDestinationProcessor(GithubImageDestinationProcessor())
                }
            }).usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                    val spansFactory = object : SpanFactory {
                        override fun getSpans(
                            configuration: MarkwonConfiguration, props: RenderProps
                        ): Any {
                            // this is the destination of image, you can additionally process it
                            val url = ImageProps.DESTINATION.require(props)

                            return LinkSpan(
                                configuration.theme(),
                                url,
                                ImageLinkResolver(configuration.linkResolver())
                            )
                        }
                    }

                    builder.appendFactory(Image::class.java, spansFactory)
                }
            }).build()

        markwon.setMarkdown(textView, md)
    }

    class ImageLinkResolver(val original: LinkResolver) : LinkResolver {
        override fun resolve(view: View, link: String) {
            // decide if you want to open gallery or anything else,
            //  here we just pass to original
            original.resolve(view, link)
        }
    }
}