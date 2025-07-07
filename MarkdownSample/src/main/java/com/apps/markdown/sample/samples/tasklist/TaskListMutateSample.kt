package com.apps.markdown.sample.samples.tasklist

import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.samples.tasklist.shared.TaskListHolder
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.ext.tasklist.TaskListItem
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.ext.tasklist.TaskListSpan

@MarkwonSampleInfo(
    id = "20200702140901",
    title = "GFM task list mutate",
    artifacts = [MarkwonArtifact.EXT_TASKLIST],
    tags = [Tag.PLUGIN]
)
class TaskListMutateSample : MarkwonTextViewSample() {
    override fun render() {
        // NB! this sample works for a single level task list,
        //  if you have multiple levels, then see the `TaskListMutateNestedSample`

        val markwon: Markwon = Markwon.builder(context).usePlugin(TaskListPlugin.create(context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureSpansFactory(builder: io.noties.markwon.MarkwonSpansFactory.Builder) {
                    // obtain origin task-list-factory
                    val origin = builder.getFactory(TaskListItem::class.java)
                    if (origin == null) {
                        return
                    }

                    builder.setFactory(
                        TaskListItem::class.java,
                        object : SpanFactory {
                            override fun getSpans(
                                configuration: MarkwonConfiguration, props: RenderProps
                            ): Any {
                                val span: TaskListSpan =
                                    origin.getSpans(configuration, props) as TaskListSpan

                                return arrayOf(
                                    span, TaskListToggleSpan(span)
                                )
                            }

                        },
                    )
                }
            }).build()

        markwon.setMarkdown(textView, TaskListHolder.MD)
    }

    internal class TaskListToggleSpan(private val span: TaskListSpan) : ClickableSpan() {
        override fun onClick(widget: android.view.View) {
            // toggle span (this is a mere visual change)
            span.isDone = !span.isDone
            // request visual update
            widget.invalidate()

            // it must be a TextView
            val textView = widget as android.widget.TextView
            // it must be spanned
            val spanned: Spanned = textView.text as Spanned

            // actual text of the span (this can be used along with the  `span`)
            spanned.subSequence(
                spanned.getSpanStart(this), spanned.getSpanEnd(this)
            )
        }

        override fun updateDrawState(ds: TextPaint) {
        }
    }
}
