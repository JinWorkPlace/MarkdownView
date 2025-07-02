package io.noties.markwon.recycler

import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import io.noties.markwon.Markwon
import io.noties.markwon.utils.NoCopySpannableFactory
import org.commonmark.node.Node

/**
 * @since 3.0.0
 */
class SimpleEntry(
    @param:LayoutRes private val layoutResId: Int,
    @param:IdRes private val textViewIdRes: Int
) : MarkwonAdapter.Entry<Node, SimpleEntry.Holder>() {
    // small cache for already rendered nodes
    private val cache: MutableMap<Node, Spanned> = HashMap()

    override fun createHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(textViewIdRes, inflater.inflate(layoutResId, parent, false))
    }

    override fun bindHolder(markwon: Markwon, holder: Holder, node: Node) {
        var spanned = cache[node]
        if (spanned == null) {
            spanned = markwon.render(node)
            cache.put(node, spanned)
        }
        markwon.setParsedMarkdown(holder.textView, spanned)
    }

    override fun clear() {
        cache.clear()
    }

    class Holder(@IdRes textViewIdRes: Int, itemView: View) : MarkwonAdapter.Holder(itemView) {
        val textView: TextView

        init {
            val textView: TextView
            if (textViewIdRes == 0) {
                check(itemView is TextView) {
                    "TextView is not root of layout " +
                            "(specify TextView ID explicitly): " + itemView
                }
                textView = itemView
            } else {
                textView = requireView<TextView>(textViewIdRes)
            }
            this.textView = textView
            this.textView.setSpannableFactory(NoCopySpannableFactory.getInstance())
        }
    }

    companion object {
        /**
         * Create [SimpleEntry] that has TextView as the root view of
         * specified layout.
         */
        @JvmStatic
        fun createTextViewIsRoot(@LayoutRes layoutResId: Int): SimpleEntry {
            return SimpleEntry(layoutResId, 0)
        }

        @JvmStatic
        fun create(@LayoutRes layoutResId: Int, @IdRes textViewIdRes: Int): SimpleEntry {
            return SimpleEntry(layoutResId, textViewIdRes)
        }
    }
}
