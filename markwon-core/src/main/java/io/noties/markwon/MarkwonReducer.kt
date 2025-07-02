package io.noties.markwon

import org.commonmark.node.LinkReferenceDefinition
import org.commonmark.node.Node

/**
 * @since 3.0.0
 */
abstract class MarkwonReducer {
    abstract fun reduce(node: Node): MutableList<Node?>


    internal class DirectChildren : MarkwonReducer() {
        override fun reduce(root: Node): MutableList<Node?> {
            val list: MutableList<Node?>

            // we will extract all blocks that are direct children of Document
            var node = root.getFirstChild()

            // please note, that if there are no children -> we will return a list with
            // single element (which was supplied)
            if (node == null) {
                list = mutableListOf<Node?>(root)
            } else {
                list = ArrayList<Node?>()

                var temp: Node?

                while (node != null) {
                    // @since 4.5.0 do not include LinkReferenceDefinition node (would result
                    //  in empty textView if rendered in recycler-view)
                    if (node !is LinkReferenceDefinition) {
                        list.add(node)
                    }
                    temp = node.getNext()
                    node.unlink()
                    node = temp
                }
            }

            return list
        }
    }

    companion object {
        /**
         * @return direct children of supplied Node. In the most usual case
         * will return all BlockNodes of a Document
         */
        fun directChildren(): MarkwonReducer {
            return DirectChildren()
        }
    }
}
