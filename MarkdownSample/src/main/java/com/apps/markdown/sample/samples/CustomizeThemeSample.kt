package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon

@MarkwonSampleInfo(
    id = "20200629123617",
    title = "Customize theme",
    description = "Customize `MarkwonTheme` styling",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.STYLE, Tag.THEME, Tag.PLUGIN]
)
class CustomizeThemeSample : MarkwonTextViewSample() {
    override fun render() {
        val md = "`A code` that is rendered differently\n\n```\nHello!\n```"

        val markwon: Markwon = Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureTheme(builder: io.noties.markwon.core.MarkwonTheme.Builder) {
                builder.codeBackgroundColor(android.graphics.Color.BLACK)
                    .codeTextColor(android.graphics.Color.RED)
            }
        }).build()

        markwon.setMarkdown(textView, md)
    }
}
