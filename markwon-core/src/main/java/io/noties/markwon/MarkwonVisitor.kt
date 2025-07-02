package io.noties.markwon

import org.commonmark.node.Node
import org.commonmark.node.Visitor

/**
 * Configurable visitor of parsed markdown. Allows visiting certain (registered) nodes without
 * need to create own instance of this class.
 *
 * @see Builder.on
 * @see MarkwonPlugin.configureVisitor
 * @since 3.0.0
 */
interface MarkwonVisitor : Visitor {
    /**
     * @see Builder.on
     */
    fun interface NodeVisitor<N : Node> {
        fun visit(visitor: MarkwonVisitor, node: N)
    }

    /**
     * Primary purpose is to control the spacing applied before/after certain blocks, which
     * visitors are created elsewhere
     *
     * @since 4.3.0
     */
    interface BlockHandler {
        fun blockStart(visitor: MarkwonVisitor, node: Node)

        fun blockEnd(visitor: MarkwonVisitor, node: Node)
    }

    interface Builder {
        /**
         * @param node        to register
         * @param nodeVisitor [NodeVisitor] to be used or null to ignore previously registered
         * visitor for this node
         */
        fun <N : Node> on(node: Class<N>, nodeVisitor: NodeVisitor<in N>?): Builder

        /**
         * @param blockHandler to handle block start/end
         * @see BlockHandler
         *
         * @see BlockHandlerDef
         *
         * @since 4.3.0
         */
        fun blockHandler(blockHandler: BlockHandler): Builder

        fun build(configuration: MarkwonConfiguration, renderProps: RenderProps): MarkwonVisitor
    }

    fun configuration(): MarkwonConfiguration

    fun renderProps(): RenderProps

    fun builder(): SpannableBuilder

    /**
     * Visits all children of supplied node.
     *
     * @param node to visit
     */
    fun visitChildren(node: Node)

    /**
     * Executes a check if there is further content available.
     *
     * @param node to check
     * @return boolean indicating if there are more nodes after supplied one
     */
    fun hasNext(node: Node): Boolean

    /**
     * This method **ensures** that further content will start at a new line. If current
     * last character is already a new line, then it won\'t do anything.
     */
    fun ensureNewLine()

    /**
     * This method inserts a new line without any condition checking (unlike [.ensureNewLine]).
     */
    fun forceNewLine()

    /**
     * Helper method to call `builder().length()`
     *
     * @return current length of underlying [SpannableBuilder]
     */
    fun length(): Int

    /**
     * Clears state of visitor (both [RenderProps] and [SpannableBuilder] will be cleared
     */
    fun clear()

    /**
     * Sets `spans` to underlying [SpannableBuilder] from *start*
     * to *[SpannableBuilder.length]*.
     *
     * @param start start position of spans
     * @param spans to apply
     */
    fun setSpans(start: Int, spans: Any?)

    /**
     * Helper method to obtain and apply spans for supplied Node. Internally queries [SpanFactory]
     * for the node (via [MarkwonSpansFactory.require] thus throwing an exception
     * if there is no [SpanFactory] registered for the node).
     *
     * @param node  to retrieve [SpanFactory] for
     * @param start start position for further [.setSpans] call
     * @see .setSpansForNodeOptional
     */
    fun <N : Node> setSpansForNode(node: N, start: Int)

    /**
     * The same as [.setSpansForNode] but can be used in situations when there is
     * no access to a Node instance (for example in HTML rendering which doesn\'t have markdown Nodes).
     *
     * @see .setSpansForNode
     */
    fun <N : Node> setSpansForNode(node: Class<N>, start: Int)

    // does not throw if there is no SpanFactory registered for this node
    /**
     * Helper method to apply spans from a [SpanFactory] **if** it\'s registered in
     * [MarkwonSpansFactory] instance. Otherwise ignores this call (no spans will be applied).
     * If there is a need to ensure that specified `node` has a [SpanFactory] registered,
     * then [.setSpansForNode] can be used. [.setSpansForNode] internally
     * uses [MarkwonSpansFactory.require]. This method uses [MarkwonSpansFactory.get].
     *
     * @see .setSpansForNode
     */
    fun <N : Node> setSpansForNodeOptional(node: N, start: Int)

    /**
     * The same as [.setSpansForNodeOptional] but can be used in situations when
     * there is no access to a Node instance (for example in HTML rendering).
     *
     * @see .setSpansForNodeOptional
     */
    @Suppress("unused")
    fun <N : Node> setSpansForNodeOptional(node: Class<N>, start: Int)

    /**
     * @since 4.3.0
     */
    fun blockStart(node: Node)

    /**
     * @since 4.3.0
     */
    fun blockEnd(node: Node)
}
