package com.apps.markdown.sample.samples.latex

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.samples.latex.shared.LatexHolder
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.latex.JLatexMathPlugin.BuilderConfigure
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin

@MarkwonSampleInfo(
    id = "20200701093504",
    title = "LaTeX inline/block different text size",
    artifacts = [MarkwonArtifact.EXT_LATEX, MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.RENDERING]
)
class LatexDifferentTextSizesSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# LaTeX different text sizes\n" + "inline: " + LatexHolder.LATEX_BANGLE + ", okay and block:\n" + "$$\n" + "" + LatexHolder.LATEX_BANGLE + "\n" + "$$\n" + "that's it"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(MarkwonInlineParserPlugin.create()).usePlugin(
                JLatexMathPlugin.create(
                    textView.textSize * 0.75f,
                    textView.textSize * 1.50f,
                    object : BuilderConfigure {
                        override fun configureBuilder(builder: JLatexMathPlugin.Builder) {
                            builder.inlinesEnabled(true)
                        }
                    })
            ).build()

        markwon.setMarkdown(textView, md)
    }
}
