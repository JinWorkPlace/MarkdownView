package com.apps.markdown.sample.samples.movementmethod

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.movement.MovementMethodPlugin

@MarkwonSampleInfo(
    id = "20200627081631",
    title = "MovementMethodPlugin",
    description = "Plugin to control movement method",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.MOVEMENT_METHOD, Tag.LINKS, Tag.PLUGIN]
)
class MovementMethodPluginSample : MarkwonTextViewSample() {
    override fun render() {
        val md = """
      # MovementMethodPlugin
      `MovementMethodPlugin` can be used to apply movement method 
      explicitly. Including specific case to disable implicit movement 
      method which is applied when `TextView.getMovementMethod()` 
      returns `null`. A [link](https://github.com)
    """.trimIndent()

        val markwon = Markwon.builder(context).usePlugin(MovementMethodPlugin.link()).build()

        markwon.setMarkdown(textView, md)
    }
}