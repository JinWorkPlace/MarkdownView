package io.noties.markwon.syntax

interface SyntaxHighlight {
    fun highlight(info: String?, code: String): CharSequence
}
