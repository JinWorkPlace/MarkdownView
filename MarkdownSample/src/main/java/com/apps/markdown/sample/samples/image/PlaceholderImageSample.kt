package com.apps.markdown.sample.samples.image

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.apps.markdown.sample.R
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.ImagesPlugin.ImagesConfigure

@MarkwonSampleInfo(
    id = "20200630165504",
    title = "Image with placeholder",
    artifacts = [MarkwonArtifact.IMAGE],
    tags = [Tag.IMAGE]
)
class PlaceholderImageSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "![image](https://github.com/dcurtis/markdown-mark/raw/master/png/1664x1024-solid.png)"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(ImagesPlugin.create(object : ImagesConfigure {
                override fun configureImages(plugin: ImagesPlugin) {
                    plugin.placeholderProvider(object : ImagesPlugin.PlaceholderProvider {
                        override fun providePlaceholder(drawable: AsyncDrawable): Drawable? {
                            // by default drawable intrinsic size will be used
                            //  otherwise bounds can be applied explicitly
                            return ContextCompat.getDrawable(
                                context, R.drawable.ic_android_black_24dp
                            )
                        }
                    })
                }
            })).build()

        markwon.setMarkdown(textView, md)
    }
}
