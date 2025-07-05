package com.apps.markdown.sample.samples.html

import android.text.TextUtils
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.graphics.toColorInt
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.html.CssInlineStyleParser
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.html.HtmlPlugin.HtmlConfigure
import io.noties.markwon.html.tag.SimpleTagHandler

private const val TAG = "HtmlCssStyleParser"

@MarkwonSampleInfo(
    id = "20210118155530",
    title = "CSS attributes in HTML",
    description = "Parse CSS attributes of HTML tags with `CssInlineStyleParser`",
    artifacts = [MarkwonArtifact.HTML],
    tags = [Tag.HTML]
)
class HtmlCssStyleParserSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "# CSS\n\n" + "<span style=\"background-color: #ff0000;\">this has red background</span> and then\n\n" + "this <span style=\"color: #00ff00;\">is green</span>"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(HtmlPlugin.create(object : HtmlConfigure {
                override fun configureHtml(plugin: HtmlPlugin) {
                    plugin.addHandler(SpanTagHandler())
                }
            })).build()

        markwon.setMarkdown(textView, md)
    }

    private class SpanTagHandler : SimpleTagHandler() {
        override fun getSpans(
            configuration: MarkwonConfiguration,
            renderProps: RenderProps,
            tag: io.noties.markwon.html.HtmlTag
        ): Any? {
            val style = tag.attributes()["style"] ?: return null
            if (TextUtils.isEmpty(style)) {
                return null
            }

            var color = 0
            var backgroundColor = 0

            for (property in CssInlineStyleParser.create().parse(style)) {
                when (property.key()) {
                    "color" -> color = property.value().toColorInt()
                    "background-color" -> backgroundColor = property.value().toColorInt()

                    else -> Log.i(TAG, "unexpected CSS property: $property")
                }
            }

            val spans: MutableList<Any> = ArrayList(3)

            if (color != 0) {
                spans.add(ForegroundColorSpan(color))
            }
            if (backgroundColor != 0) {
                spans.add(BackgroundColorSpan(backgroundColor))
            }

            return spans.toTypedArray()
        }

        override fun supportedTags(): MutableCollection<String> {
            return mutableSetOf("span")
        }
    }
}
