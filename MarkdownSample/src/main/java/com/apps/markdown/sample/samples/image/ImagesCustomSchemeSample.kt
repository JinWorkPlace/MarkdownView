package com.apps.markdown.sample.samples.image

import androidx.core.net.toUri
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.image.ImageItem
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.SchemeHandler
import io.noties.markwon.image.network.NetworkSchemeHandler

@MarkwonSampleInfo(
    id = "20200629124201",
    title = "Image destination custom scheme",
    description = ("Example of handling custom scheme " + "(`https`, `ftp`, `whatever`, etc.) for images destination URLs " + "with `ImagesPlugin`"),
    artifacts = [MarkwonArtifact.IMAGE],
    tags = [Tag.IMAGE]
)
class ImagesCustomSchemeSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "![image](myownscheme://en.wikipedia.org/static/images/project-logos/enwiki-2x.png)"

        val markwon: Markwon = Markwon.builder(context).usePlugin(ImagesPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configure(registry: io.noties.markwon.MarkwonPlugin.Registry) {
                    // use registry.require to obtain a plugin, does also
                    // a runtime validation if this plugin is registered

                    registry.require<ImagesPlugin>(
                        ImagesPlugin::class.java,
                        io.noties.markwon.MarkwonPlugin.Action { plugin: ImagesPlugin ->
                            plugin.addSchemeHandler(object : SchemeHandler() {
                                // it's a sample only, most likely you won't need to
                                // use existing scheme-handler, this for demonstration purposes only
                                val handler: NetworkSchemeHandler = NetworkSchemeHandler.create()

                                override fun handle(
                                    raw: String, uri: android.net.Uri
                                ): ImageItem {
                                    // just replace it with https for the sack of sample
                                    val url = raw.replace("myownscheme", "https")
                                    return handler.handle(url, url.toUri())
                                }

                                override fun supportedSchemes(): MutableCollection<String> {
                                    return mutableSetOf("myownscheme")
                                }
                            })
                        })
                }
            }).build()

        markwon.setMarkdown(textView, md)
    }
}
