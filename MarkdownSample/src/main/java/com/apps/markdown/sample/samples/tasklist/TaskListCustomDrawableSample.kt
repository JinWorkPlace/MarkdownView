package com.apps.markdown.sample.samples.tasklist

import androidx.core.content.ContextCompat
import com.apps.markdown.sample.R
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.samples.tasklist.shared.TaskListHolder
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tasklist.TaskListPlugin

@MarkwonSampleInfo(
    id = "20200702140749",
    title = "GFM task list custom drawable",
    artifacts = [MarkwonArtifact.EXT_TASKLIST],
    tags = [Tag.PLUGIN]
)
class TaskListCustomDrawableSample : MarkwonTextViewSample() {
    public override fun render() {
        val drawable = java.util.Objects.requireNonNull<android.graphics.drawable.Drawable>(
            ContextCompat.getDrawable(context, R.drawable.custom_task_list)
        )

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(TaskListPlugin.create(drawable)).build()

        markwon.setMarkdown(textView, TaskListHolder.MD)
    }
}
