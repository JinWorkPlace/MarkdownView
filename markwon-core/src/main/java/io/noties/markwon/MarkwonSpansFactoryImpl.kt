package io.noties.markwon

import org.commonmark.node.Node
import java.util.Collections

/**
 * @since 3.0.0
 */
internal class MarkwonSpansFactoryImpl(
    private val factories: MutableMap<Class<out Node>, SpanFactory>
) : MarkwonSpansFactory {
    override fun <N : Node> get(node: Class<N>): SpanFactory? {
        return factories[node]
    }

    override fun <N : Node> require(node: Class<N>): SpanFactory {
        val f: SpanFactory = get(node)!!
        return f
    }

    internal class BuilderImpl : MarkwonSpansFactory.Builder {
        private val factories: MutableMap<Class<out Node>, SpanFactory> = HashMap(3)

        override fun <N : Node> setFactory(
            node: Class<N>, factory: SpanFactory?
        ): MarkwonSpansFactory.Builder {
            if (factory == null) {
                factories.remove(node)
            } else {
                factories.put(node, factory)
            }
            return this
        }

        @Deprecated("")
        override fun <N : Node> addFactory(
            node: Class<N>, factory: SpanFactory
        ): MarkwonSpansFactory.Builder {
            return prependFactory(node, factory)
        }

        override fun <N : Node> appendFactory(
            node: Class<N>, factory: SpanFactory
        ): MarkwonSpansFactory.Builder {
            val existing = factories[node]
            if (existing == null) {
                factories.put(node, factory)
            } else {
                if (existing is CompositeSpanFactory) {
                    existing.factories.add(0, factory)
                } else {
                    val compositeSpanFactory = CompositeSpanFactory(factory, existing)
                    factories.put(node, compositeSpanFactory)
                }
            }
            return this
        }

        override fun <N : Node> prependFactory(
            node: Class<N>, factory: SpanFactory
        ): MarkwonSpansFactory.Builder {
            // if there is no factory registered for this node -> just add it
            val existing = factories[node]
            if (existing == null) {
                factories.put(node, factory)
            } else {
                // existing span factory can be of CompositeSpanFactory at this point -> append to it
                if (existing is CompositeSpanFactory) {
                    existing.factories.add(factory)
                } else {
                    // if it's not composite at this point -> make it
                    val compositeSpanFactory = CompositeSpanFactory(existing, factory)
                    factories.put(node, compositeSpanFactory)
                }
            }
            return this
        }

        override fun <N : Node> getFactory(node: Class<N>): SpanFactory? {
            return factories[node]
        }

        override fun <N : Node> requireFactory(node: Class<N>): SpanFactory {
            val factory: SpanFactory? = getFactory(node)
            if (factory == null) {
                throw NullPointerException(node.name)
            }
            return factory
        }

        override fun build(): MarkwonSpansFactory {
            return MarkwonSpansFactoryImpl(
                Collections.unmodifiableMap(
                    factories
                )
            )
        }
    }

    internal class CompositeSpanFactory(first: SpanFactory, second: SpanFactory) : SpanFactory {
        @JvmField
        val factories: MutableList<SpanFactory> = ArrayList(3)

        init {
            this.factories.add(first)
            this.factories.add(second)
        }

        override fun getSpans(configuration: MarkwonConfiguration, props: RenderProps): Any {
            // please note that we do not check it factory itself returns an array of spans,
            // as this behaviour is supported now (previously we supported only a single-level array)
            val length = factories.size
            val out = arrayOfNulls<Any>(length)
            for (i in 0..<length) {
                out[i] = factories[i].getSpans(configuration, props)
            }
            return out
        }
    }
}
