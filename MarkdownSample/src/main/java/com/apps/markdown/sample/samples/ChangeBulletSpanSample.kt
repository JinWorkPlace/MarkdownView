package com.apps.markdown.sample.samples

import android.text.style.BulletSpan
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
import io.noties.markwon.core.CoreProps
import io.noties.markwon.ext.tasklist.TaskListPlugin
import org.commonmark.node.ListItem

@MarkwonSampleInfo(
    id = "20201208150530",
    title = "Change bullet span",
    description = "Use a different span implementation to render bullet lists",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.RENDERING, Tag.SPAN_FACTORY, Tag.SPAN]
)
class ChangeBulletSpanSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "* one\n" + "* two\n" + "* three\n" + "* * four\n" + "  * five\n\n" + "- [ ] and task?\n" + "- [x] it is"

        val markwon: Markwon = Markwon.builder(context).usePlugin(TaskListPlugin.create(context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                    // store original span factory (provides both bullet and ordered lists)

                    val original = builder.getFactory(ListItem::class.java)

                    builder.setFactory(
                        ListItem::class.java, object : SpanFactory {
                            override fun getSpans(
                                configuration: MarkwonConfiguration, props: RenderProps
                            ): Any {
                                if (CoreProps.LIST_ITEM_TYPE.require(props) == CoreProps.ListItemType.BULLET) {
                                    // additionally inspect bullet level
                                    val level: Int = CoreProps.BULLET_LIST_ITEM_LEVEL.require(props)
                                    return BulletSpan()
                                }

                                return original!!.getSpans(configuration, props)
                            }
                        })
                }
            }).build()

        markwon.setMarkdown(textView, md)
    }
}
