package io.noties.markwon

import org.commonmark.node.Node

/**
 * Class that controls what spans are used for certain Nodes.
 *
 * @see SpanFactory
 *
 * @since 3.0.0
 */
interface MarkwonSpansFactory {
    /**
     * Returns registered [SpanFactory] or `null` if a factory for this node type
     * is not registered. There is [.require] method that will throw an exception
     * if required [SpanFactory] is not registered, thus making return type `non-null`
     *
     * @param node type of the node
     * @return registered [SpanFactory] or null if it\'s not registered
     * @see .require
     */
    fun <N : Node> get(node: Class<N>): SpanFactory?

    fun <N : Node> require(node: Class<N>): SpanFactory


    interface Builder {
        fun <N : Node> setFactory(node: Class<N>, factory: SpanFactory?): Builder

        /**
         * Helper method to add a [SpanFactory] for a Node. This method will merge existing
         * [SpanFactory] with the specified one.
         *
         * @since 3.0.1
         */
        @Deprecated(
            """4.2.2 consider using {@link #appendFactory(Class, SpanFactory)} or
          {@link #prependFactory(Class, SpanFactory)} methods for more explicit factory ordering.
          `addFactory` behaved like {@link #prependFactory(Class, SpanFactory)}, so
          this method call can be replaced with it"""
        )
        fun <N : Node> addFactory(node: Class<N>, factory: SpanFactory): Builder

        /**
         * Append a factory to existing one (or make the first one for specified node). Specified factory
         * will be called **after** original (if present) factory. Can be used to
         * *change* behavior or original span factory.
         *
         * @since 4.2.2
         */
        fun <N : Node> appendFactory(node: Class<N>, factory: SpanFactory): Builder

        /**
         * Prepend a factory to existing one (or make the first one for specified node). Specified factory
         * will be called <string>before</string> original (if present) factory.
         *
         * @since 4.2.2
         */
        fun <N : Node> prependFactory(node: Class<N>, factory: SpanFactory): Builder

        /**
         * Can be useful when *enhancing* an already defined SpanFactory with another one.
         */
        fun <N : Node> getFactory(node: Class<N>): SpanFactory?

        /**
         * To obtain current [SpanFactory] associated with specified node. Can be used
         * when SpanFactory must be present for node. If it\'s not added/registered a runtime
         * exception will be thrown
         *
         * @see .getFactory
         * @since 3.0.1
         */
        fun <N : Node> requireFactory(node: Class<N>): SpanFactory?

        fun build(): MarkwonSpansFactory?
    }
}
