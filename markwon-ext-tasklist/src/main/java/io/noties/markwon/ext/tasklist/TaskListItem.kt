package io.noties.markwon.ext.tasklist

import org.commonmark.node.CustomBlock

/**
 * @since 1.0.1
 */
class TaskListItem(@JvmField val isDone: Boolean) : CustomBlock() {
    override fun toString(): String {
        return "TaskListItem{isDone=$isDone}"
    }
}
