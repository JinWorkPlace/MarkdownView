package io.noties.markwon.ext.tasklist

import android.text.TextUtils
import io.noties.markwon.utils.ParserUtils
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.node.Text
import org.commonmark.parser.PostProcessor
import java.util.regex.Pattern

// @since 4.6.0
// Hint taken from commonmark-ext-task-list-items artifact
internal class TaskListPostProcessor : PostProcessor {
    override fun process(node: Node): Node {
        val visitor = TaskListVisitor()
        node.accept(visitor)
        return node
    }

    private class TaskListVisitor : AbstractVisitor() {
        override fun visit(listItem: ListItem) {
            // Takes first child and checks if it is Text (we are looking for exact `[xX\s]` without any formatting)
            val child = listItem.firstChild
            // check if it is paragraph (can contain text)
            if (child is Paragraph) {
                val node = child.firstChild
                if (node is Text) {
                    val textNode = node
                    val matcher = REGEX_TASK_LIST_ITEM.matcher(textNode.literal)

                    if (matcher.matches()) {
                        val checked = matcher.group(1)
                        val isChecked = "x" == checked || "X" == checked

                        val taskListItem = TaskListItem(isChecked)

                        val paragraph = Paragraph()

                        // insert before list item (directly before inside parent)
                        listItem.insertBefore(taskListItem)

                        // append the rest of matched text (can be empty)
                        val restMatchedText = matcher.group(2)
                        if (!TextUtils.isEmpty(restMatchedText)) {
                            paragraph.appendChild(Text(restMatchedText))
                        }

                        // move all the rest children (from the first paragraph)
                        ParserUtils.moveChildren(paragraph, node)

                        // append our created paragraph
                        taskListItem.appendChild(paragraph)

                        // move all the rest children from the listItem (further nested lists, etc)
                        ParserUtils.moveChildren(taskListItem, child)

                        // remove list item from node
                        listItem.unlink()

                        // visit taskListItem children
                        visitChildren(taskListItem)
                        return
                    }
                }
            }
            visitChildren(listItem)
        }

        companion object {
            private val REGEX_TASK_LIST_ITEM: Pattern = Pattern.compile("^\\[([xX\\s])]\\s+(.*)")
        }
    }
}
