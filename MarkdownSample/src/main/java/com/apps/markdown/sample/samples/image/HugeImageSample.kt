package com.apps.markdown.sample.samples.image

import android.view.ViewTreeObserver
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.image.DefaultDownScalingMediaDecoder
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.ImagesPlugin.ImagesConfigure

@MarkwonSampleInfo(
    id = "20210118165230",
    title = "Huge image downscaling",
    description = "Downscale displayed images with `BitmapOptions` 2 step rendering " + "(measure, downscale), use `DefaultDownScalingMediaDecoder`",
    artifacts = [MarkwonArtifact.IMAGE],
    tags = [Tag.IMAGE]
)
class HugeImageSample : MarkwonTextViewSample() {
    override fun render() {
        // NB! this is based on the width of the widget. In case you have big vertical
        //  images (with big vertical dimension, use some reasonable value or fallback to real OpenGL
        //  maximum, see: https://stackoverflow.com/questions/15313807/android-maximum-allowed-width-height-of-bitmap

        val width: Int = textView.width
        if (width > 0) {
            renderWithMaxWidth(width)
            return
        }

        textView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val w: Int = textView.width
                if (w > 0) {
                    renderWithMaxWidth(w)

                    val observer: ViewTreeObserver = textView.viewTreeObserver
                    if (observer.isAlive) {
                        observer.removeOnPreDrawListener(this)
                    }
                }
                return true
            }
        })
    }

    private fun renderWithMaxWidth(maxWidth: Int) {
        val md =
            "" + "# Huge image\n\n" + "![this is alt](https://otakurevolution.com/storyimgs/falldog/GundamTimeline/Falldogs_GundamTimeline_v13_April2020.png)\n\n" + "hey!"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(ImagesPlugin.create(object : ImagesConfigure {
                override fun configureImages(plugin: ImagesPlugin) {
                    plugin.defaultMediaDecoder(DefaultDownScalingMediaDecoder.create(maxWidth, 0))
                }
            })).build()

        markwon.setMarkdown(textView, md)
    }
}
