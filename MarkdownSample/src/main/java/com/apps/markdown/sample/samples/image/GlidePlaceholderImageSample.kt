package com.apps.markdown.sample.samples.image

import android.graphics.drawable.Drawable
import com.apps.markdown.sample.R
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.Target
import io.noties.markwon.Markwon
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.glide.GlideImagesPlugin

@MarkwonSampleInfo(
    id = "20200630170241",
    title = "Glide image with placeholder",
    artifacts = [MarkwonArtifact.IMAGE_GLIDE],
    tags = [Tag.IMAGE]
)
class GlidePlaceholderImageSample : MarkwonTextViewSample() {
    public override fun render() {
        val md =
            "[![undefined](https://img.youtube.com/vi/gs1I8_m4AOM/0.jpg)](https://www.youtube.com/watch?v=gs1I8_m4AOM)"

        val context: android.content.Context = this.context

        val markwon: Markwon = Markwon.builder(context)
            .usePlugin(GlideImagesPlugin.create(object : GlideImagesPlugin.GlideStore {
                override fun load(drawable: AsyncDrawable): RequestBuilder<Drawable?> {
                    return Glide.with(context).load(drawable.destination)
                        .placeholder(R.drawable.ic_home_black_36dp)
                }

                override fun cancel(target: Target<*>) {
                    Glide.with(context).clear(target)
                }
            })).build()

        markwon.setMarkdown(textView, md)
    }
}
