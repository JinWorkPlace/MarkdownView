package com.apps.markdown.sample.samples.table

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin

@MarkwonSampleInfo(
    id = "20200702140041",
    title = "LaTeX inside table",
    description = "Usage of LaTeX formulas inside markdown tables",
    artifacts = [MarkwonArtifact.EXT_LATEX, MarkwonArtifact.EXT_TABLES, MarkwonArtifact.IMAGE],
    tags = [Tag.IMAGE]
)
class TableLatexSample : MarkwonTextViewSample() {
    override fun render() {
        var latex = "\\begin{array}{cc}"
        latex += "\\fbox{\\text{A framed box with \\textdbend}}&\\shadowbox{\\text{A shadowed box}}\\cr"
        latex += "\\doublebox{\\text{A double framed box}}&\\ovalbox{\\text{An oval framed box}}\\cr"
        latex += "\\end{array}"

        val md =
            "| HEADER | HEADER |\n|:----:|:----:|\n| ![Build](https://github.com/noties/Markwon/workflows/Build/badge.svg) | Build |\n| Stable | ![stable](https://img.shields.io/maven-central/v/io.noties.markwon/core.svg?label=stable) |\n| BIG | $$$latex$$ |\n\n"

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(MarkwonInlineParserPlugin.create())
                .usePlugin(ImagesPlugin.create()).usePlugin(
                    JLatexMathPlugin.create(
                        textView.textSize,
                        object : JLatexMathPlugin.BuilderConfigure {
                            override fun configureBuilder(builder: JLatexMathPlugin.Builder) {
                                builder.inlinesEnabled(true)
                            }
                        },
                    )
                ).usePlugin(TablePlugin.create(context)).build()

        markwon.setMarkdown(textView, md)
    }
}
