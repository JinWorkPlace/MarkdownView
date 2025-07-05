package com.apps.markdown.sample.samples.html

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon

@MarkwonSampleInfo(
    id = "20200630171424",
    title = "Disable HTML",
    description = "Disable HTML via replacing special `<` and `>` symbols",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.HTML, Tag.RENDERING, Tag.PARSING, Tag.PLUGIN]
)
class HtmlDisableSanitizeSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "# Html <b>disabled</b>\n\n" + "<em>emphasis <strong>strong</strong>\n\n" + "<p>paragraph <img src='hey.jpg' /></p>\n\n" + "<test></test>\n\n" + "<test>"

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun processMarkdown(markdown: String): String {
                return markdown.replace("<".toRegex(), "&lt;").replace(">".toRegex(), "&gt;")
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}
