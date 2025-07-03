package io.noties.markwon.ext.tasklist

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.MarkwonVisitor
import org.commonmark.parser.Parser

/**
 * @since 3.0.0
 */
class TaskListPlugin private constructor(private val drawable: Drawable) : AbstractMarkwonPlugin() {
    override fun configureParser(builder: Parser.Builder) {
        builder.postProcessor(TaskListPostProcessor())
    }

    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(TaskListItem::class.java, TaskListSpanFactory(drawable))
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        builder
            .on(
                TaskListItem::class.java,
                MarkwonVisitor.NodeVisitor { visitor, taskListItem ->
                    val length = visitor.length()

                    visitor.visitChildren(taskListItem)

                    TaskListProps.DONE.set(visitor.renderProps(), taskListItem.isDone)

                    visitor.setSpansForNode(taskListItem, length)

                    if (visitor.hasNext(taskListItem)) {
                        visitor.ensureNewLine()
                    }
                })
    }

    companion object {
        /**
         * Supplied Drawable must be stateful ([Drawable.isStateful] returns true). If a task
         * is marked as done, then this drawable will be updated with an `int[] { android.R.attr.state_checked }`
         * as the state, otherwise an empty array will be used. This library provides a ready to be
         * used Drawable: [TaskListDrawable]
         *
         * @see TaskListDrawable
         */
        fun create(drawable: Drawable): TaskListPlugin {
            return TaskListPlugin(drawable)
        }

        fun create(context: Context): TaskListPlugin {
            // by default we will be using link color for the checkbox color
            // & window background as a checkMark color

            val linkColor: Int = resolve(context, android.R.attr.textColorLink)
            val backgroundColor: Int = resolve(context, android.R.attr.colorBackground)

            return TaskListPlugin(TaskListDrawable(linkColor, linkColor, backgroundColor))
        }

        fun create(
            @ColorInt checkedFillColor: Int,
            @ColorInt normalOutlineColor: Int,
            @ColorInt checkMarkColor: Int
        ): TaskListPlugin {
            return TaskListPlugin(
                TaskListDrawable(
                    checkedFillColor,
                    normalOutlineColor,
                    checkMarkColor
                )
            )
        }

        private fun resolve(context: Context, @AttrRes attr: Int): Int {
            val typedValue = TypedValue()
            val attrs = intArrayOf(attr)
            val typedArray = context.obtainStyledAttributes(typedValue.data, attrs)
            try {
                return typedArray.getColor(0, 0)
            } finally {
                typedArray.recycle()
            }
        }
    }
}
