package io.noties.markwon.html

/**
 * @since 2.0.0
 */
abstract class MarkwonHtmlParser {
    fun interface FlushAction<T> {
        fun apply(tags: MutableList<T>)
    }

    abstract fun <T> processFragment(
        output: T, htmlFragment: String
    ) where T : Appendable, T : CharSequence

    /**
     * After this method exists a [MarkwonHtmlParser] will clear internal state for stored tags.
     * If you wish to process them further after this method exists create own copy of supplied
     * collection.
     *
     * @param documentLength known document length. This value is used to close all non-closed tags.
     * If you wish to keep them open (do not force close at the end of a
     * document pass here [HtmlTag.NO_END]. Later non-closed tags
     * can be detected by calling [HtmlTag.isClosed]
     * @param action         [FlushAction] to be called with resulting tags ([HtmlTag.Inline])
     */
    abstract fun flushInlineTags(
        documentLength: Int, action: FlushAction<HtmlTag.Inline>
    )

    /**
     * After this method exists a [MarkwonHtmlParser] will clear internal state for stored tags.
     * If you wish to process them further after this method exists create own copy of supplied
     * collection.
     *
     * @param documentLength known document length. This value is used to close all non-closed tags.
     * If you wish to keep them open (do not force close at the end of a
     * document pass here [HtmlTag.NO_END]. Later non-closed tags
     * can be detected by calling [HtmlTag.isClosed]
     * @param action         [FlushAction] to be called with resulting tags ([HtmlTag.Block])
     */
    abstract fun flushBlockTags(
        documentLength: Int, action: FlushAction<HtmlTag.Block>
    )

    abstract fun reset()
}
