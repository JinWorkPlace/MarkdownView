package com.apps.markdown.sample.samples.latex

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.samples.latex.shared.LatexHolder
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin

@MarkwonSampleInfo(
    id = "20200701120848",
    title = "LaTeX default text color",
    description = "LaTeX will use text color of `TextView` by default",
    artifacts = [MarkwonArtifact.EXT_LATEX],
    tags = [Tag.RENDERING]
)
class LatexDefaultTextColorSample : MarkwonTextViewSample() {
    override fun render() {
        textView.setTextColor(android.graphics.Color.RED)

        val md =
            "" + "# LaTeX default text color\n" + "$$\n" + "" + LatexHolder.LATEX_LONG_DIVISION + "\n" + "$$\n" + ""

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(JLatexMathPlugin.create(textView.textSize)).build()

        markwon.setMarkdown(textView, md)
    }
}
