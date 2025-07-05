package com.apps.markdown.sample.samples.latex

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin

@MarkwonSampleInfo(
    id = "20200701090618",
    title = "LaTeX omega symbol",
    description = "Bug rendering omega symbol in LaTeX",
    artifacts = [MarkwonArtifact.EXT_LATEX, MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.RENDERING, Tag.KNOWN_BUG]
)
class LatexOmegaSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Block\n\n" + "$$\n" + "\\Omega\n" + "$$\n\n" + "# Inline\n\n" + "$$\\Omega$$"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(MarkwonInlineParserPlugin.create()).usePlugin(
                JLatexMathPlugin.create(
                    textView.textSize,
                    object : JLatexMathPlugin.BuilderConfigure {
                        override fun configureBuilder(builder: JLatexMathPlugin.Builder) {
                            builder.inlinesEnabled(true)
                        }
                    },
                )
            ).build()

        markwon.setMarkdown(textView, md)
    }
}
