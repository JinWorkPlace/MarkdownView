package com.apps.markdown.sample.samples

import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory

@MarkwonSampleInfo(
    id = "20200702101224",
    title = "Remove link underline",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.LINKS, Tag.RENDERING, Tag.SPAN]
)
class LinkRemoveUnderlineSample : MarkwonTextViewSample() {
    override fun render() {
        val md = "" + "There are a lot of [links](#) [here](#)"

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                builder.appendFactory(
                    org.commonmark.node.Link::class.java,
                    object : SpanFactory {
                        override fun getSpans(
                            configuration: MarkwonConfiguration, props: RenderProps
                        ): Any {
                            return RemoveUnderlineSpan()
                        }
                    },
                )
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}

internal class RemoveUnderlineSpan : CharacterStyle(), UpdateAppearance {
    override fun updateDrawState(tp: TextPaint) {
        tp.isUnderlineText = false
    }
}
