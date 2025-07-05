package com.apps.markdown.sample.samples.latex

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.samples.latex.shared.LatexHolder
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin

@MarkwonSampleInfo(
    id = "20200630200257",
    title = "LaTex block",
    description = "Render LaTeX block",
    artifacts = [MarkwonArtifact.EXT_LATEX],
    tags = [Tag.RENDERING]
)
class LatexBlockSample : MarkwonTextViewSample() {
    override fun render() {
        val md = "" + "# LaTeX\n" + "$$\n" + "" + LatexHolder.LATEX_ARRAY + "\n" + "$$"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(JLatexMathPlugin.create(textView.textSize)).build()

        markwon.setMarkdown(textView, md)
    }
}
