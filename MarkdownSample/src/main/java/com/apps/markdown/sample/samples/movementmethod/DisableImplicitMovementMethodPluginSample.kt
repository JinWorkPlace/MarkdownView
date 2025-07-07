package com.apps.markdown.sample.samples.movementmethod

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.movement.MovementMethodPlugin

@MarkwonSampleInfo(
    id = "20200629121803",
    title = "Disable implicit movement method via plugin",
    description = "Disable implicit movement method via `MovementMethodPlugin`",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.LINKS, Tag.MOVEMENT_METHOD, Tag.RECYCLE_VIEW]
)
class DisableImplicitMovementMethodPluginSample : MarkwonTextViewSample() {
    override fun render() {
        val md = """
      # Disable implicit movement method via plugin
      We can disable implicit movement method via `MovementMethodPlugin` &mdash;
      [link-that-is-not-clickable](https://noties.io)
    """.trimIndent()

        val markwon = Markwon.builder(context).usePlugin(MovementMethodPlugin.none()).build()

        markwon.setMarkdown(textView, md)
    }
}