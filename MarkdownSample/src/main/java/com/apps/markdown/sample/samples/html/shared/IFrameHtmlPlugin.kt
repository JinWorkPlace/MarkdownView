package com.apps.markdown.sample.samples.html.shared

import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.RenderProps
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.tag.SimpleTagHandler
import io.noties.markwon.image.ImageProps
import io.noties.markwon.image.ImageSize
import org.commonmark.node.Image

class IFrameHtmlPlugin : AbstractMarkwonPlugin() {
    override fun configure(registry: MarkwonPlugin.Registry) {
        registry.require(
            HtmlPlugin::class.java,
            MarkwonPlugin.Action { htmlPlugin: HtmlPlugin -> htmlPlugin.addHandler(EmbedTagHandler()) })
    }

    private class EmbedTagHandler : SimpleTagHandler() {
        override fun getSpans(
            configuration: MarkwonConfiguration, renderProps: RenderProps, tag: HtmlTag
        ): Any? {
            val imageSize = ImageSize(
                ImageSize.Dimension(640f, "px"), ImageSize.Dimension(480f, "px")
            )
            ImageProps.IMAGE_SIZE.set(renderProps, imageSize)

            ImageProps.DESTINATION.set(
                renderProps,
                "https://img1.ak.crunchyroll.com/i/spire2/d7b1d6bc7563224388ef5ffc04a967581589950464_full.jpg"
            )

            return configuration.spansFactory().require(Image::class.java)
                .getSpans(configuration, renderProps)
        }

        override fun supportedTags(): MutableCollection<String> {
            return mutableSetOf("iframe")
        }
    }
}
