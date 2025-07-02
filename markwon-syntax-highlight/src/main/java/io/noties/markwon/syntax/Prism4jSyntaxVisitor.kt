package io.noties.markwon.syntax

import android.text.SpannableStringBuilder
import io.noties.prism4j.AbsVisitor
import io.noties.prism4j.Prism4j

internal data class Prism4jSyntaxVisitor(
    private val language: String,
    private val theme: Prism4jTheme,
    private val builder: SpannableStringBuilder
) : AbsVisitor() {
    override fun visitText(text: Prism4j.Text) {
        builder.append(text.literal())
    }

    override fun visitSyntax(syntax: Prism4j.Syntax) {
        val start = builder.length
        visit(syntax.children())
        val end = builder.length

        if (end != start) {
            theme.apply(language, syntax, builder, start, end)
        }
    }
}
