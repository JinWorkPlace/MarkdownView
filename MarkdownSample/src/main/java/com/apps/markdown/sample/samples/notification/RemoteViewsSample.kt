package com.apps.markdown.sample.samples.notification

import android.graphics.Color
import android.graphics.Typeface
import android.text.style.BackgroundColorSpan
import android.text.style.BulletSpan
import android.text.style.QuoteSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.widget.RemoteViews
import com.apps.markdown.sample.R
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
import io.noties.markwon.core.CoreProps
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.node.BlockQuote
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.Heading
import org.commonmark.node.ListItem
import org.commonmark.node.StrongEmphasis

@MarkwonSampleInfo(
    id = "20200702090140",
    title = "RemoteViews in notification",
    description = "Display markdown with platform (system) spans in notification via `RemoteViews`",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.HACK]
)
class RemoteViewsSample : MarkwonTextViewSample() {
    override fun render() {
        val md = "# Heading 1\n" +
                //      "## Heading 2\n" +
                //      "### Heading 3\n" +
                //      "#### Heading 4\n" +
                //      "##### Heading 5\n" +
                //      "###### Heading 6\n" +
                "**bold _italic_ bold** `code` [link](#) ~~strike~~\n" + "* Bullet 1\n" + "* * Bullet 2\n" + "  * Bullet 3\n" + "> A quote **here**"

        val headingSizes = floatArrayOf(
            2f, 1.5f, 1.17f, 1f, .83f, .67f,
        )

        val bulletGapWidth = (8 * context.resources.displayMetrics.density + 0.5f).toInt()

        val markwon: Markwon = Markwon.builder(context).usePlugin(StrikethroughPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                val headingFactory = object : SpanFactory {
                    override fun getSpans(
                        configuration: MarkwonConfiguration, props: RenderProps
                    ): Any {
                        return arrayOf<Any>(
                            StyleSpan(Typeface.BOLD), RelativeSizeSpan(
                                headingSizes[CoreProps.HEADING_LEVEL.require(
                                    props
                                ) - 1]
                            )
                        )
                    }
                }

                val blockQuoteFactory = object : SpanFactory {
                    override fun getSpans(
                        configuration: MarkwonConfiguration, props: RenderProps
                    ): Any {
                        return QuoteSpan()
                    }
                }

                val typefaceBoldFactory = object : SpanFactory {
                    override fun getSpans(
                        configuration: MarkwonConfiguration, props: RenderProps
                    ): Any {
                        return StyleSpan(Typeface.BOLD)
                    }
                }
                val typefaceItalicFactory = object : SpanFactory {
                    override fun getSpans(
                        configuration: MarkwonConfiguration, props: RenderProps
                    ): Any {
                        return StyleSpan(Typeface.ITALIC)
                    }
                }

                val codeSpanFactory = object : SpanFactory {
                    override fun getSpans(
                        configuration: MarkwonConfiguration, props: RenderProps
                    ): Any {
                        return arrayOf<Any>(
                            BackgroundColorSpan(Color.GRAY), TypefaceSpan("monospace")
                        )
                    }
                }


                val bulletSpanFactory = object : SpanFactory {
                    override fun getSpans(
                        configuration: MarkwonConfiguration, props: RenderProps
                    ): Any {
                        return BulletSpan(bulletGapWidth)
                    }
                }

                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                    builder.setFactory(
                        Heading::class.java, headingFactory
                    ).setFactory(
                        StrongEmphasis::class.java, typefaceBoldFactory
                    ).setFactory(
                        Emphasis::class.java, typefaceItalicFactory
                    ).setFactory(
                        Code::class.java, codeSpanFactory
                    ).setFactory(
                        Strikethrough::class.java, object : SpanFactory {
                            override fun getSpans(
                                configuration: MarkwonConfiguration, props: RenderProps
                            ): Any {
                                return StrikethroughSpan()
                            }
                        }).setFactory(
                        ListItem::class.java, bulletSpanFactory
                    ).setFactory(
                        BlockQuote::class.java, blockQuoteFactory
                    )
                }
            }).build()

        val remoteViews = RemoteViews(context.packageName, R.layout.sample_remote_view)
        remoteViews.setTextViewText(R.id.text_view, markwon.toMarkdown(md))

        NotificationUtils.display(context, remoteViews)
    }
}
