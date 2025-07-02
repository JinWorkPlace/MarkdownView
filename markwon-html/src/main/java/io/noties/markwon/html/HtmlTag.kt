package io.noties.markwon.html

/**
 * @see Inline
 *
 * @see Block
 *
 * @since 2.0.0
 */
interface HtmlTag {
    /**
     * @return normalized tag name (lower-case)
     */
    fun name(): String

    /**
     * @return index at which this tag starts
     */
    fun start(): Int

    /**
     * @return index at which this tag ends
     */
    fun end(): Int

    /**
     * @return flag indicating if this tag has no content (when start == end)
     */
    val isEmpty: Boolean

    /**
     * @return flag indicating if this tag is closed (has valid start and end)
     * @see .NO_END
     */
    val isClosed: Boolean

    fun attributes(): MutableMap<String, String>

    /**
     * @see Inline
     */
    val isInline: Boolean

    /**
     * @see Block
     */
    val isBlock: Boolean

    val asInline: Inline

    val asBlock: Block

    /**
     * Represents *really* inline HTML tags (unlile commonmark definitions)
     */
    interface Inline : HtmlTag

    /**
     * Represents HTML block tags. Please note that all tags that are not inline should be
     * considered as block tags
     */
    interface Block : HtmlTag {
        /**
         * @return parent [Block] or null if there is no parent (this block is at root level)
         */
        fun parent(): Block

        /**
         * @return list of children
         */
        fun children(): MutableList<Block>

        /**
         * @return a flag indicating if this [Block] is at the root level (shortcut to calling:
         * `parent() == null`
         */
        val isRoot: Boolean
    }

    companion object {
        const val NO_END: Int = -1
    }
}
