package io.noties.markwon.ext.tasklist

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.style.LeadingMarginSpan
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.utils.LeadingMarginUtils

/**
 * @since 1.0.1
 */
class TaskListSpan(
    private val theme: MarkwonTheme, private val drawable: Drawable,
    /**
     * Update [.isDone] property of this span. Please note that this is merely a visual change
     * which is not changing underlying text in any means.
     *
     * @since 2.0.1
     */
    // @since 2.0.1 field is NOT final (to allow mutation)
    var isDone: Boolean
) : LeadingMarginSpan {
    /**
     * @since 2.0.1
     */

    init {
        this.isDone = isDone
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return theme.getBlockMargin()
    }

    override fun drawLeadingMargin(
        c: Canvas,
        p: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence?,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout?
    ) {
        if (!first
            || !LeadingMarginUtils.selfStart(start, text, this)
        ) {
            return
        }

        val descent = p.descent()
        val ascent = p.ascent()

        val save = c.save()
        try {
            val width = theme.getBlockMargin()
            val height = (descent - ascent + 0.5f).toInt()

            val w = (width * .75f + .5f).toInt()
            val h = (height * .75f + .5f).toInt()

            drawable.setBounds(0, 0, w, h)

            if (drawable.isStateful) {
                val state: IntArray = if (isDone) {
                    STATE_CHECKED
                } else {
                    STATE_NONE
                }
                drawable.setState(state)
            }
            val l: Int = if (dir > 0) {
                x + ((width - w) / 2)
            } else {
                x - ((width - w) / 2) - w
            }

            val t = (baseline + ascent + 0.5f).toInt() + ((height - h) / 2)

            c.translate(l.toFloat(), t.toFloat())
            drawable.draw(c)
        } finally {
            c.restoreToCount(save)
        }
    }

    companion object {
        private val STATE_CHECKED = intArrayOf(android.R.attr.state_checked)

        private val STATE_NONE = IntArray(0)
    }
}
