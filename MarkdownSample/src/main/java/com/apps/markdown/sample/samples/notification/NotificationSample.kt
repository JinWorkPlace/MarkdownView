package com.apps.markdown.sample.samples.notification

import android.graphics.Color
import android.graphics.Typeface
import android.text.style.BackgroundColorSpan
import android.text.style.BulletSpan
import android.text.style.QuoteSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.samples.notification.shared.NotificationUtils
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import org.commonmark.node.BlockQuote
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.ListItem
import org.commonmark.node.StrongEmphasis

@MarkwonSampleInfo(
    id = "20200701130729",
    title = "Markdown in Notification",
    description = "Proof of concept of using `Markwon` with `android.app.Notification`",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.HACK]
)
class NotificationSample : MarkwonTextViewSample() {
    override fun render() {
        // supports:
        // * bold -> StyleSpan(BOLD)
        // * italic -> StyleSpan(ITALIC)
        // * quote -> QuoteSpan()
        // * strikethrough -> StrikethroughSpan()
        // * bullet list -> BulletSpan()

        // * link -> is styled but not clickable
        // * code -> typeface monospace works, background is not

        val md =
            "" + "**bold _bold-italic_ bold** ~~strike~~ `code` [link](#)\n\n" + "* bullet-one\n" + "* * bullet-two\n" + "  * bullet-three\n\n" + "> a quote\n\n" + ""

        val markwon: Markwon = Markwon.builder(context).usePlugin(StrikethroughPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                val emphasisFactory = object : SpanFactory {
                    override fun getSpans(
                        configuration: MarkwonConfiguration, props: RenderProps
                    ): Any {
                        return StyleSpan(Typeface.ITALIC)
                    }
                }

                val strongEmphasisFactory = object : SpanFactory {
                    override fun getSpans(
                        configuration: MarkwonConfiguration, props: RenderProps
                    ): Any {
                        return StyleSpan(Typeface.BOLD)
                    }
                }

                val blockQuoteFactory = object : SpanFactory {
                    override fun getSpans(
                        configuration: MarkwonConfiguration, props: RenderProps
                    ): Any {
                        return QuoteSpan()
                    }
                }

                val bulletSpanFactory = object : SpanFactory {
                    override fun getSpans(
                        configuration: MarkwonConfiguration, props: RenderProps
                    ): Any {
                        return BulletSpan()
                    }
                }


                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                    builder.setFactory<Emphasis>(
                        node = Emphasis::class.java, factory = emphasisFactory
                    ).setFactory<StrongEmphasis>(
                        StrongEmphasis::class.java, factory = strongEmphasisFactory
                    ).setFactory<BlockQuote>(
                        node = BlockQuote::class.java, factory = blockQuoteFactory
                    ).setFactory<Strikethrough>(
                        Strikethrough::class.java, object : SpanFactory {
                            override fun getSpans(
                                configuration: MarkwonConfiguration, props: RenderProps
                            ): Any {
                                return StrikethroughSpan()
                            }
                        }.setFactory<Code>(
                            Code::class.java,
                            io.noties.markwon.SpanFactory { configuration: MarkwonConfiguration?, props: RenderProps? ->
                                arrayOf<Any>(
                                    BackgroundColorSpan(Color.GRAY), TypefaceSpan("monospace")
                                )
                            }) // NB! both ordered and bullet list items
                            .setFactory<ListItem>(
                                ListItem::class.java, bulletSpanFactory
                            )
                    )
                }
            }).build()

        markwon.setMarkdown(textView, md)

        NotificationUtils.display(context, markwon.toMarkdown(md))
    }
}
