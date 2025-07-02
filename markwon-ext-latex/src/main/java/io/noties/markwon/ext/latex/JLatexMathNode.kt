package io.noties.markwon.ext.latex

import org.commonmark.node.CustomNode

/**
 * @since 4.3.0
 */
class JLatexMathNode : CustomNode() {
    private var latex: String? = null

    fun latex(): String? {
        return latex
    }

    fun latex(latex: String?) {
        this.latex = latex
    }
}
