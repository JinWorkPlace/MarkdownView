package io.noties.markwon.ext.tasklist

import android.graphics.drawable.Drawable
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory

class TaskListSpanFactory(private val drawable: Drawable) : SpanFactory {
    override fun getSpans(configuration: MarkwonConfiguration, props: RenderProps): Any? {
        return TaskListSpan(
            configuration.theme(),
            drawable,
            TaskListProps.DONE.get(props, false)
        )
    }
}
