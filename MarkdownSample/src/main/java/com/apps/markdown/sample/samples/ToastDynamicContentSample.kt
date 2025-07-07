package com.apps.markdown.sample.samples

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.image.ImagesPlugin


@MarkwonSampleInfo(
    id = "20200627074017",
    title = "Markdown in Toast (with dynamic content)",
    description = "Display markdown in a `android.widget.Toast` with dynamic content (image)",
    artifacts = [MarkwonArtifact.CORE, MarkwonArtifact.IMAGE],
    tags = [Tag.TOAST, Tag.HACK]
)
class ToastDynamicContentSample : MarkwonTextViewSample() {
    override fun render() {
        val md = """
      # Head!
      
      ![alt](${BuildConfig.GIT_REPOSITORY}/raw/master/art/markwon_logo.png)
      
      Do you see an image? ☝️
    """.trimIndent()

        val markwon = Markwon.builder(context).usePlugin(ImagesPlugin.create()).build()

        val markdown = markwon.toMarkdown(md)

        val toast = Toast.makeText(context, markdown, Toast.LENGTH_LONG)

        // try to obtain textView
        val textView = toast.textView
        if (textView != null) {
            markwon.setParsedMarkdown(textView, markdown)
        }

        // finally show toast (at this point, if we didn't find TextView it will still
        // present markdown, just without dynamic content (image))
        toast.show()
    }
}

private val Toast.textView: TextView?
    get() {

        fun find(view: View?): TextView? {

            if (view is TextView) {
                return view
            }

            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    val textView = find(view.getChildAt(i))
                    if (textView != null) {
                        return textView
                    }
                }
            }

            return null
        }

        return find(view)
    }