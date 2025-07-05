package com.apps.markdown.sample.samples.latex

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.samples.latex.shared.LatexHolder
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin

@MarkwonSampleInfo(
    id = "20200701090335",
    title = "LaTeX blocks in legacy mode",
    description = "Sample using _legacy_ LaTeX block parsing (pre `4.3.0` Markwon version)",
    artifacts = [MarkwonArtifact.EXT_LATEX],
    tags = [Tag.RENDERING]
)
class LatexLegacySample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# LaTeX legacy\n" + "There are no inlines in previous versions, only blocks:\n" + "$$\n" + "" + LatexHolder.LATEX_BOXES + "\n" + "$$\n" + "yeah"

        val markwon: Markwon = Markwon.builder(context).usePlugin(
            JLatexMathPlugin.create(
                textView.textSize,
                object : JLatexMathPlugin.BuilderConfigure {
                    override fun configureBuilder(builder: JLatexMathPlugin.Builder) {
                        builder.blocksLegacy(true)
                    }
                },
            )
        ).build()

        markwon.setMarkdown(textView, md)
    }
}
