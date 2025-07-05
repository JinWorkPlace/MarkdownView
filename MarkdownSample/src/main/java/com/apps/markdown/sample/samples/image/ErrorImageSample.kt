package com.apps.markdown.sample.samples.image

import androidx.core.content.ContextCompat
import com.apps.markdown.sample.R
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.image.ImagesPlugin

@MarkwonSampleInfo(
    id = "20200630165828",
    title = "Image error handler",
    artifacts = [MarkwonArtifact.IMAGE],
    tags = [Tag.IMAGE]
)
class ErrorImageSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "![error](https://github.com/dcurtis/markdown-mark/raw/master/png/______1664x1024-solid.png)"

        val markwon: Markwon =
            Markwon.builder(context) // error handler additionally allows to log/inspect errors during image loading
                .usePlugin(ImagesPlugin.create(object : ImagesPlugin.ImagesConfigure {
                    override fun configureImages(plugin: ImagesPlugin) {
                        plugin.errorHandler(object : ImagesPlugin.ErrorHandler {
                            override fun handleError(
                                url: String, throwable: Throwable
                            ): android.graphics.drawable.Drawable? {
                                return ContextCompat.getDrawable(
                                    context, R.drawable.ic_home_black_36dp
                                )
                            }
                        })
                    }
                })).build()

        markwon.setMarkdown(textView, md)
    }
}
