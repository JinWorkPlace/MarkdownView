package io.noties.markwon.editor

import io.noties.markwon.Markwon

/**
 * @see EditHandler
 *
 * @see io.noties.markwon.editor.handler.EmphasisEditHandler
 *
 * @see io.noties.markwon.editor.handler.StrongEmphasisEditHandler
 *
 * @since 4.2.0
 */
abstract class AbstractEditHandler<T> : EditHandler<T> {
    override fun init(markwon: Markwon) {
    }
}
