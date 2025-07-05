package com.apps.markdown.sample.samples.html

import android.text.style.AlignmentSpan
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.html.tag.SimpleTagHandler

@MarkwonSampleInfo(
    id = "20200630114630",
    title = "Align HTML tag",
    description = "Implement custom HTML tag handling",
    artifacts = [MarkwonArtifact.HTML],
    tags = [Tag.RENDERING, Tag.SPAN, Tag.HTML]
)
class HtmlAlignSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "<align center>We are centered</align>\n" + "\n" + "<align end>We are at the end</align>\n" + "\n" + "<align>We should be at the start</align>\n" + "\n"


        val markwon: Markwon = Markwon.builder(context).usePlugin(HtmlPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configure(registry: io.noties.markwon.MarkwonPlugin.Registry) {
                    registry.require(
                        HtmlPlugin::class.java,
                        io.noties.markwon.MarkwonPlugin.Action { htmlPlugin: HtmlPlugin ->
                            htmlPlugin.addHandler(AlignTagHandler())
                        })
                }
            }).build()

        markwon.setMarkdown(textView, md)
    }
}

internal class AlignTagHandler : SimpleTagHandler() {
    override fun getSpans(
        configuration: MarkwonConfiguration,
        renderProps: RenderProps,
        tag: io.noties.markwon.html.HtmlTag
    ): Any? {
        val alignment: android.text.Layout.Alignment

        // html attribute without value, <align center></align>
        if (tag.attributes().containsKey("center")) {
            alignment = android.text.Layout.Alignment.ALIGN_CENTER
        } else if (tag.attributes().containsKey("end")) {
            alignment = android.text.Layout.Alignment.ALIGN_OPPOSITE
        } else {
            // empty value or any other will make regular alignment
            alignment = android.text.Layout.Alignment.ALIGN_NORMAL
        }

        return AlignmentSpan.Standard(alignment)
    }

    override fun supportedTags(): MutableCollection<String> {
        return mutableSetOf("align")
    }
}
