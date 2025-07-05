package com.apps.markdown.sample.samples.latex

import androidx.core.content.ContextCompat
import com.apps.markdown.sample.R
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin

@MarkwonSampleInfo(
    id = "20200701122624",
    title = "LaTeX error handling",
    description = "Log error when parsing LaTeX and display error drawable",
    artifacts = [MarkwonArtifact.EXT_LATEX],
    tags = [Tag.RENDERING]
)
class LatexErrorSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# LaTeX with error\n" + "$$\n" + "\\sum_{i=0}^\\infty x \\cdot 0 \\rightarrow \\iMightNotExist{0}\n" + "$$\n\n" + "must **not** be rendered"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(MarkwonInlineParserPlugin.create()).usePlugin(
                JLatexMathPlugin.create(
                    textView.textSize, object : JLatexMathPlugin.BuilderConfigure {
                        override fun configureBuilder(builder: JLatexMathPlugin.Builder) {
                            builder.inlinesEnabled(true)
                            builder.errorHandler(object : JLatexMathPlugin.ErrorHandler {
                                override fun handleError(
                                    latex: String, error: Throwable
                                ): android.graphics.drawable.Drawable? {
                                    return ContextCompat.getDrawable(
                                        context, R.drawable.ic_android_black_24dp
                                    )
                                }
                            })
                        }
                    })
            ).build()

        markwon.setMarkdown(textView, md)
    }
}
