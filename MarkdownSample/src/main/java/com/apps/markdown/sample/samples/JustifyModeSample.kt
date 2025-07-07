package com.apps.markdown.sample.samples

import android.text.Spanned
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.image.AsyncDrawableScheduler
import io.noties.markwon.image.ImagesPlugin

@MarkwonSampleInfo(
    id = "20200826084338",
    title = "Justify text",
    description = "Justify text with `justificationMode` argument on Oreo (>= 26)",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.RENDERING]
)
class JustifyModeSample : MarkwonTextViewSample() {
    @android.annotation.SuppressLint("WrongConstant")
    override fun render() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            textView.justificationMode = android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
        }

        val md =
            "" + "# Justify\n\n" + "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis rutrum orci at aliquet dapibus. Quisque laoreet fermentum bibendum. Suspendisse euismod nisl vel sapien viverra faucibus. Nulla vel neque volutpat, egestas dui ac, consequat elit. Donec et interdum massa. Quisque porta ornare posuere. Nam at ante a felis facilisis tempus eu et erat. Curabitur auctor mauris eget purus iaculis vulputate.\n\n" + "> Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis rutrum orci at aliquet dapibus. Quisque laoreet fermentum bibendum. Suspendisse euismod nisl vel sapien viverra faucibus. Nulla vel neque volutpat, egestas dui ac, consequat elit. Donec et interdum massa. Quisque porta ornare posuere. Nam at ante a felis facilisis tempus eu et erat. Curabitur auctor mauris eget purus iaculis vulputate.\n\n" + "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis rutrum orci at aliquet dapibus. Quisque laoreet fermentum bibendum. Suspendisse euismod nisl vel sapien viverra faucibus. Nulla vel neque volutpat, egestas dui ac, consequat elit. Donec et interdum massa. **Quisque porta ornare posuere.** Nam at ante a felis facilisis tempus eu et erat. Curabitur auctor mauris eget purus iaculis vulputate.\n\n" + ""

        val markwon: Markwon = Markwon.builder(context).usePlugin(ImagesPlugin.create()).build()

        val spanned: Spanned = markwon.toMarkdown(md)

        // NB! the call to `setText` without arguments
        textView.text = spanned

        // if a plugin relies on `afterSetText` then we must manually call it,
        //  for example images are scheduled this way:
        AsyncDrawableScheduler.schedule(textView)

        // cannot use that
        markwon.setMarkdown(textView, md)

        return
    }
}
