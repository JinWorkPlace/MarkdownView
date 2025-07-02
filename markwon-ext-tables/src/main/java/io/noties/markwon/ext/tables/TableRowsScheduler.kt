package io.noties.markwon.ext.tables

import android.text.Spanned
import android.text.TextUtils
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.widget.TextView

internal object TableRowsScheduler {
    @JvmStatic
    fun schedule(view: TextView) {
        val spans = extract(view)
        if (spans != null && spans.isNotEmpty()) {
            if (view.getTag(R.id.markwon_tables_scheduler) == null) {
                val listener: OnAttachStateChangeListener = object : OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        unschedule(view)
                        view.removeOnAttachStateChangeListener(this)
                        view.setTag(R.id.markwon_tables_scheduler, null)
                    }
                }
                view.addOnAttachStateChangeListener(listener)
                view.setTag(R.id.markwon_tables_scheduler, listener)
            }

            val invalidator: TableRowSpan.Invalidator = object : TableRowSpan.Invalidator {
                // @since 4.1.0
                // let's stack-up invalidation calls (so invalidation happens,
                // but not with each table-row-span draw call)
                val runnable: Runnable = Runnable { view.text = view.getText() }

                override fun invalidate() {
                    // @since 4.1.0 post invalidation (combine multiple calls)
                    view.removeCallbacks(runnable)
                    view.post(runnable)
                }
            }

            for (span in spans) {
                span.invalidator(invalidator)
            }
        }
    }

    @JvmStatic
    fun unschedule(view: TextView) {
        val spans = extract(view)
        if (spans != null
            && spans.isNotEmpty()
        ) {
            for (span in spans) {
                span.invalidator(null)
            }
        }
    }

    private fun extract(view: TextView): Array<TableRowSpan>? {
        val out: Array<TableRowSpan>?
        val text = view.getText()
        out = if (!TextUtils.isEmpty(text) && text is Spanned) {
            text.getSpans(0, text.length, TableRowSpan::class.java)
        } else {
            null
        }
        return out
    }
}
