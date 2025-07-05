package com.apps.markdown.sample.samples.latex

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.samples.latex.shared.LatexHolder
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin

@MarkwonSampleInfo(
    id = "20200701085820",
    title = "LaTeX inline",
    description = "Display LaTeX inline",
    artifacts = [MarkwonArtifact.EXT_LATEX, MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.RENDERING]
)
class LatexInlineSample : MarkwonTextViewSample() {
    public override fun render() {
        val md =
            "" + "# LaTeX inline\n" + "hey = $$" + LatexHolder.LATEX_BANGLE + "$$,\n" + "that's it!"

        // inlines must be explicitly enabled and require `MarkwonInlineParserPlugin`
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
