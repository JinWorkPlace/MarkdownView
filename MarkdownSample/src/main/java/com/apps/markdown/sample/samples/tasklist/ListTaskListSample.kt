package com.apps.markdown.sample.samples.tasklist

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tasklist.TaskListPlugin

@MarkwonSampleInfo(
    id = "20200902174132",
    title = "Task list items with other lists",
    description = "Mix of task list items with other lists (bullet and ordered)",
    artifacts = [MarkwonArtifact.EXT_TASKLIST],
    tags = [Tag.LISTS]
)
class ListTaskListSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "- [ ] Task **1**\n" + "- [ ] _Task_ 2\n" + "- [ ] Task 3\n" + "  - Sub Task 3.1\n" + "  - Sub Task 3.2\n" + "    * [X] Sub Task 4.1\n" + "    * [X] Sub Task 4.2\n" + "- [ ] Task 4\n" + "  - [ ] Sub Task 3.1\n" + "  - [ ] Sub Task 3.2"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(TaskListPlugin.create(context)).build()

        markwon.setMarkdown(textView, md)
    }
}
