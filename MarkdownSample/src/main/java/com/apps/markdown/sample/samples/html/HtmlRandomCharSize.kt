package com.apps.markdown.sample.samples.html

import android.text.style.AbsoluteSizeSpan
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.html.MarkwonHtmlRenderer

@MarkwonSampleInfo(
    id = "20200630114923",
    title = "Random char size HTML tag",
    description = "Implementation of a custom HTML tag handler " + "that assigns each character a random size",
    artifacts = [MarkwonArtifact.HTML],
    tags = [Tag.RENDERING, Tag.SPAN, Tag.HTML]
)
class HtmlRandomCharSize : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "<random-char-size>\n" + "This message should have a jumpy feeling because of different sizes of characters\n" + "</random-char-size>\n\n"

        val markwon: Markwon = Markwon.builder(context).usePlugin(HtmlPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configure(registry: io.noties.markwon.MarkwonPlugin.Registry) {
                    registry.require(
                        HtmlPlugin::class.java,
                        io.noties.markwon.MarkwonPlugin.Action { htmlPlugin: HtmlPlugin ->
                            htmlPlugin.addHandler(
                                RandomCharSize(
                                    java.util.Random(42L), textView.textSize
                                )
                            )
                        })
                }
            }).build()

        markwon.setMarkdown(textView, md)
    }
}

internal class RandomCharSize(
    private val random: java.util.Random, private val base: Float
) : io.noties.markwon.html.TagHandler() {
    override fun handle(
        visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, tag: io.noties.markwon.html.HtmlTag
    ) {
        val builder: SpannableBuilder = visitor.builder()

        // text content is already added, we should only apply spans
        var i = tag.start()
        val end = tag.end()
        while (i < end) {
            val size = (base * (random.nextFloat() + 0.5f) + 0.5f).toInt()
            builder.setSpan(AbsoluteSizeSpan(size, false), i, i + 1)
            i++
        }
    }

    override fun supportedTags(): MutableCollection<String> {
        return mutableSetOf("random-char-size")
    }
}
