package com.apps.markdown.sample.samples.plugins

import android.widget.TextView
import com.apps.markdown.sample.R
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.samples.plugins.shared.AnchorHeadingPlugin
import com.apps.markdown.sample.samples.plugins.shared.AnchorHeadingPlugin.ScrollTo
import io.noties.markwon.Markwon

@MarkwonSampleInfo(
    id = "20200629130728",
    title = "Anchor plugin",
    description = "HTML-like anchor links plugin, which scrolls to clicked anchor",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.LINKS, Tag.ANCHOR, Tag.PLUGIN]
)
class AnchorSample : MarkwonTextViewSample() {
    override fun render() {
        val lorem: String = context.getString(R.string.lorem)
        val md = "Hello [there](#there)!\n\n\n$lorem\n\n# There!\n\n$lorem"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(AnchorHeadingPlugin(object : ScrollTo {
                override fun scrollTo(view: TextView, top: Int) {
                    scrollView.smoothScrollTo(0, top)
                }
            })).build()

        markwon.setMarkdown(textView, md)
    }
}

