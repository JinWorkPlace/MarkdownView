package com.apps.markdown.sample.samples.tasklist

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.samples.tasklist.shared.TaskListHolder
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tasklist.TaskListPlugin

@MarkwonSampleInfo(
    id = "20200702140352",
    title = "GFM task list",
    description = "Github Flavored Markdown (GFM) task list extension",
    artifacts = [MarkwonArtifact.EXT_TASKLIST],
    tags = [Tag.PLUGIN]
)
class TaskListSample : MarkwonTextViewSample() {
    override fun render() {
        val markwon: Markwon =
            Markwon.builder(context).usePlugin(TaskListPlugin.create(context)).build()

        markwon.setMarkdown(textView, TaskListHolder.MD)
    }
}
