package com.apps.markdown.sample.samples.image

import android.graphics.drawable.Drawable
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.noties.markwon.Markwon
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.glide.GlideImagesPlugin

@MarkwonSampleInfo(
    id = "20200820071942",
    title = "Glide GIF",
    artifacts = [MarkwonArtifact.IMAGE_GLIDE],
    tags = [Tag.IMAGE]
)
class GlideGifImageSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "# Glide GIF\n" + "![gif-image](https://upload.wikimedia.org/wikipedia/commons/2/2c/Rotating_earth_%28large%29.gif) " + "and some other resource: ![image](https://github.com/dcurtis/markdown-mark/raw/master/png/208x128-solid.png)\n\n" + "Hey: ![alt](https://picsum.photos/id/237/1024/800)"

        val markwon: Markwon = Markwon.builder(context)
            .usePlugin(GlideImagesPlugin.create(GifGlideStore(Glide.with(context)))).build()

        markwon.setMarkdown(textView, md)
    }

    private class GifGlideStore(
        private val requestManager: RequestManager
    ) : GlideImagesPlugin.GlideStore {

        override fun load(drawable: AsyncDrawable): RequestBuilder<Drawable?> {
            // NB! Strange behaviour: First time a resource is requested - it fails, the next time it loads fine
            val destination: String = drawable.destination
            return requestManager // it is enough to call this (in order to load GIF and non-GIF)
                .asDrawable().addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        // we must start GIF animation manually
                        if (resource is android.graphics.drawable.Animatable) {
                            (resource as android.graphics.drawable.Animatable).start()
                        }
                        return false
                    }
                }).load(destination)
        }

        override fun cancel(target: Target<*>) {
            requestManager.clear(target)
        }
    }
}
