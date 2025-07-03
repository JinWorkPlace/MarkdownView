package io.noties.markwon.utils

import androidx.annotation.CheckResult
import org.commonmark.node.Node
import org.commonmark.node.Visitor
import java.lang.reflect.Proxy

// utility class to print parsed Nodes hierarchy
@Suppress("unused")
object DumpNodes {
    @CheckResult
    fun dump(node: Node): String {
        return dump(node, null)
    }

    @CheckResult
    fun dump(node: Node, nodeProcessor: NodeProcessor?): String {
        val processor = nodeProcessor ?: NodeProcessorToString()

        val indent = Indent()
        val builder = StringBuilder()
        val visitor = Proxy.newProxyInstance(
            Visitor::class.java.classLoader, arrayOf<Class<*>>(Visitor::class.java)
        ) { proxy, method, args ->
            val argument = args[0] as Node

            // initial indent
            indent.appendTo(builder)

            // node info
            builder.append(processor.process(argument))

            // @since 4.6.0 check for first child instead of casting to Block
            //  (regular nodes can contain other nodes, for example Text)
            if (argument.firstChild != null) {
                builder.append(" [\n")
                indent.increment()
                visitChildren((proxy as Visitor?)!!, argument)
                indent.decrement()
                indent.appendTo(builder)
                builder.append("]\n")
            } else {
                builder.append("\n")
            }

            null
        } as Visitor
        node.accept(visitor)
        return builder.toString()
    }

    private fun visitChildren(visitor: Visitor, parent: Node) {
        var node = parent.firstChild
        while (node != null) {
            // A subclass of this visitor might modify the node, resulting in getNext returning a different node or no
            // node after visiting it. So get the next node before visiting.
            val next = node.next
            node.accept(visitor)
            node = next
        }
    }

    /**
     * Creates String representation of a node which will be used in output
     */
    interface NodeProcessor {
        fun process(node: Node): String
    }

    private class Indent {
        private var count = 0

        fun increment() {
            count += 1
        }

        fun decrement() {
            count -= 1
        }

        fun appendTo(builder: StringBuilder) {
            for (i in 0..<count) {
                builder.append(' ').append(' ')
            }
        }
    }

    private class NodeProcessorToString : NodeProcessor {
        override fun process(node: Node): String {
            return node.toString()
        }
    }
}
