package io.noties.markwon.ext.latex

import org.commonmark.node.CustomBlock

class JLatexMathBlock : CustomBlock() {
    private var latex: String? = null

    fun latex(): String? {
        return latex
    }

    fun latex(latex: String?) {
        this.latex = latex
    }
}
