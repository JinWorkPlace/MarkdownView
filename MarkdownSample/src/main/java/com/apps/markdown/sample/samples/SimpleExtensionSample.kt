package com.apps.markdown.sample.samples

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.spans.EmphasisSpan
import io.noties.markwon.core.spans.StrongEmphasisSpan
import io.noties.markwon.simple.ext.SimpleExtPlugin
import io.noties.markwon.simple.ext.SimpleExtPlugin.SimpleExtConfigure

@MarkwonSampleInfo(
    id = "20200630194335",
    title = "Delimiter processor simple-ext",
    description = "Custom delimiter processor implemented with a `SimpleExtPlugin`",
    artifacts = [MarkwonArtifact.SIMPLE_EXT],
    tags = [Tag.PARSING]
)
class SimpleExtensionSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# SimpleExt\n" + "\n" + "+let's start with `+`, ??then we can use this, and finally @@this$$??+"

        // NB! we cannot have multiple delimiter processor with the same character
        //  (even if lengths are different)
        val markwon: Markwon =
            Markwon.builder(context).usePlugin(SimpleExtPlugin.create(object : SimpleExtConfigure {
                override fun configure(plugin: SimpleExtPlugin) {
                    plugin.addExtension(1, '+', object : SpanFactory {
                        override fun getSpans(
                            configuration: MarkwonConfiguration, props: RenderProps
                        ): Any {
                            return EmphasisSpan()
                        }
                    }).addExtension(2, '?', object : SpanFactory {
                        override fun getSpans(
                            configuration: MarkwonConfiguration, props: RenderProps
                        ): Any {
                            return StrongEmphasisSpan()
                        }
                    }).addExtension(
                        2, '@', '$', object : SpanFactory {
                            override fun getSpans(
                                configuration: MarkwonConfiguration, props: RenderProps
                            ): Any {
                                return ForegroundColorSpan(Color.RED)
                            }
                        })
                }
            })).build()

        markwon.setMarkdown(textView, md)
    }
}
