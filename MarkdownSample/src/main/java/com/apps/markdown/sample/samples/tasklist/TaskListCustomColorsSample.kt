package com.apps.markdown.sample.samples.tasklist

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.samples.tasklist.shared.TaskListHolder
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tasklist.TaskListPlugin

@MarkwonSampleInfo(
    id = "20200702140536",
    title = "GFM task list custom colors",
    description = "Custom colors for task list extension",
    artifacts = [MarkwonArtifact.EXT_TASKLIST],
    tags = [Tag.PARSING]
)
class TaskListCustomColorsSample : MarkwonTextViewSample() {
    override fun render() {
        val checkedFillColor = android.graphics.Color.RED
        val normalOutlineColor = android.graphics.Color.GREEN
        val checkMarkColor = android.graphics.Color.BLUE

        val markwon: Markwon = Markwon.builder(context)
            .usePlugin(TaskListPlugin.create(checkedFillColor, normalOutlineColor, checkMarkColor))
            .build()

        markwon.setMarkdown(textView, TaskListHolder.MD)
    }
}
