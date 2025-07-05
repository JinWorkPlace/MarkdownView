package com.apps.markdown.sample.samples.latex

import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toDrawable
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import com.apps.markdown.sample.samples.latex.shared.LatexHolder
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.latex.JLatexMathTheme
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin

@MarkwonSampleInfo(
    id = "20200701121528",
    title = "LaTeX theme",
    description = "Sample of theme customization for LaTeX",
    artifacts = [MarkwonArtifact.EXT_LATEX, MarkwonArtifact.INLINE_PARSER],
    tags = [Tag.RENDERING]
)
class LatexThemeSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# LaTeX theme\n" + "Hello there $$" + LatexHolder.LATEX_BANGLE + "$$, how was it?" + "Now, what about a _different_ approach and block:\n\n" + "$$\n" + "" + LatexHolder.LATEX_LONG_DIVISION + "\n" + "$$\n\n" + "Seems **fine**"

        val blockPadding = (16 * context.resources.displayMetrics.density + 0.5f).toInt()

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(MarkwonInlineParserPlugin.create()).usePlugin(
                JLatexMathPlugin.create(
                    textView.textSize,
                    object : JLatexMathPlugin.BuilderConfigure {
                        override fun configureBuilder(builder: JLatexMathPlugin.Builder) {
                            builder.inlinesEnabled(true)
                            builder.theme().inlineBackgroundProvider(object :
                                JLatexMathTheme.BackgroundProvider {
                                override fun provide(): Drawable {
                                    return 0x200000ff.toDrawable()
                                }
                            }).inlineTextColor(android.graphics.Color.GREEN)
                                .blockBackgroundProvider(object :
                                    JLatexMathTheme.BackgroundProvider {
                                    override fun provide(): Drawable {
                                        return 0x2000ff00.toDrawable()
                                    }
                                }).blockPadding(JLatexMathTheme.Padding.all(blockPadding))
                                .blockTextColor(android.graphics.Color.RED)
                        }
                    },
                )
            ).build()

        markwon.setMarkdown(textView, md)
    }
}
