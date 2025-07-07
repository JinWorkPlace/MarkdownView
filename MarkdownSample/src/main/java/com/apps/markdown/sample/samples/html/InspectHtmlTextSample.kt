package com.apps.markdown.sample.samples.html

import android.text.style.URLSpan
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.html.HtmlPlugin.HtmlConfigure
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.MarkwonHtmlRenderer
import io.noties.markwon.html.TagHandler

@MarkwonSampleInfo(
    id = "20210201140501",
    title = "Inspect text",
    description = "Inspect text content of a `HTML` node",
    artifacts = [MarkwonArtifact.HTML],
    tags = [Tag.HTML]
)
class InspectHtmlTextSample : MarkwonTextViewSample() {
    override fun render() {
        val md = """
      <p>lorem ipsum</p>
      <div class="custom-youtube-player">https://www.youtube.com/watch?v=abcdefgh</div>
    """.trimIndent()

        val markwon = Markwon.builder(context).usePlugin(HtmlPlugin.create(object : HtmlConfigure {
            override fun configureHtml(plugin: HtmlPlugin) {
                plugin.addHandler(DivHandler())
            }
        })).build()

        markwon.setMarkdown(textView, md)
    }

    class DivHandler : TagHandler() {
        override fun handle(visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, tag: HtmlTag) {
            val attr = tag.attributes()["class"] ?: return
            if (attr.contains(CUSTOM_CLASS)) {
                val text = visitor.builder().substring(tag.start(), tag.end())
                visitor.builder().setSpan(
                    URLSpan(text), tag.start(), tag.end()
                )
            }
        }

        override fun supportedTags(): MutableCollection<String> = mutableSetOf("div")

        companion object {
            const val CUSTOM_CLASS = "custom-youtube-player"
        }
    }
}