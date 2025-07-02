package io.noties.markwon.syntax

class SyntaxHighlightNoOp : SyntaxHighlight {
    override fun highlight(info: String?, code: String): CharSequence {
        return code
    }
}
