package com.apps.markdown.sample.samples.html

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlEmptyTagReplacement
import io.noties.markwon.html.HtmlPlugin

@MarkwonSampleInfo(
    id = "20200630115725",
    title = "HTML empty tag replacement",
    description = ("Render custom content when HTML tag contents is empty, " + "in case of self-closed HTML tags or tags without content (closed " + "right after opened)"),
    artifacts = [MarkwonArtifact.HTML],
    tags = [Tag.RENDERING, Tag.HTML]
)
class HtmlEmptyTagReplacementSample : MarkwonTextViewSample() {
    override fun render() {
        val md = "" + "<empty></empty> the `<empty></empty>` is replaced?"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(HtmlPlugin.create(object : HtmlPlugin.HtmlConfigure {
                override fun configureHtml(plugin: HtmlPlugin) {
                    plugin.emptyTagReplacement(object : HtmlEmptyTagReplacement() {
                        override fun replace(tag: io.noties.markwon.html.HtmlTag): String? {
                            return if ("empty" == tag.name()) {
                                "REPLACED_EMPTY_WITH_IT"
                            } else {
                                super.replace(tag)
                            }
                        }
                    })
                }
            })).build()

        markwon.setMarkdown(textView, md)
    }
}
