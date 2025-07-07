package com.apps.markdown.sample.samples.html

import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import androidx.annotation.Px
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.MarkwonHtmlRenderer
import io.noties.markwon.html.TagHandler

@MarkwonSampleInfo(
    id = "20200630115103",
    title = "Enhance custom HTML tag",
    description = "Custom HTML tag implementation " + "that _enhances_ a part of text given start and end indices",
    artifacts = [MarkwonArtifact.HTML],
    tags = [Tag.RENDERING, Tag.SPAN, Tag.HTML]
)
class HtmlEnhanceSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "<enhance start=\"5\" end=\"12\">This is text that must be enhanced, at least a part of it</enhance>"

        val markwon: Markwon = Markwon.builder(context).usePlugin(HtmlPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configure(registry: MarkwonPlugin.Registry) {
                    registry.require(
                        HtmlPlugin::class.java, MarkwonPlugin.Action { htmlPlugin: HtmlPlugin ->
                            htmlPlugin.addHandler(EnhanceTagHandler((textView.textSize * 2 + .05f).toInt()))
                        })
                }
            }).build()

        markwon.setMarkdown(textView, md)
    }
}

internal class EnhanceTagHandler(
    @param:Px private val enhanceTextSize: Int
) : TagHandler() {
    override fun handle(
        visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, tag: HtmlTag
    ) {
        val start: Int = parsePosition(tag.attributes()["start"])
        val end: Int = parsePosition(tag.attributes()["end"])

        if (start > -1 && end > -1) {
            visitor.builder().setSpan(
                AbsoluteSizeSpan(enhanceTextSize), tag.start() + start, tag.start() + end
            )
        }
    }

    override fun supportedTags(): MutableCollection<String> {
        return mutableSetOf("enhance")
    }

    companion object {
        private fun parsePosition(value: String?): Int {
            var position: Int
            if (!TextUtils.isEmpty(value)) {
                try {
                    position = value!!.toInt()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    position = -1
                }
            } else {
                position = -1
            }
            return position
        }
    }
}
