package io.noties.markwon.utils

import org.commonmark.node.Node

/**
 * @since 4.6.0
 */
object ParserUtils {
    fun moveChildren(to: Node, from: Node) {
        var next = from.next
        var temp: Node?
        while (next != null) {
            // appendChild would unlink passed node (thus making next info un-available)
            temp = next.next
            to.appendChild(next)
            next = temp
        }
    }
}
