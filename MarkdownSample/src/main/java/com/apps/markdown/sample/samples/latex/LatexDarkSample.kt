package com.apps.markdown.sample.samples.latex

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin

@MarkwonSampleInfo(
    id = "20200701094225",
    title = "LaTeX dark",
    description = "LaTeX automatically uses `TextView` text color " + "if not configured explicitly",
    artifacts = [MarkwonArtifact.EXT_LATEX],
    tags = [Tag.RENDERING]
)
class LatexDarkSample : MarkwonTextViewSample() {
    public override fun render() {
        scrollView.setBackgroundColor(-0x1000000)
        textView.setTextColor(-0x1)

        val md =
            "" + "# LaTeX\n" + "$$\n" + "\\int \\frac{1}{x} dx = \\ln \\left| x \\right| + C\n" + "$$\n" + "text color is taken from text"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(JLatexMathPlugin.create(textView.textSize)).build()

        markwon.setMarkdown(textView, md)
    }
}
