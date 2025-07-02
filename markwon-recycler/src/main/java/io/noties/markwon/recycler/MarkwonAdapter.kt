package io.noties.markwon.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonReducer
import org.commonmark.node.Node

/**
 * Adapter to display markdown in a RecyclerView. It is done by extracting root blocks from a
 * parsed markdown document (via [MarkwonReducer] and rendering each block in a standalone RecyclerView entry. Provides
 * ability to customize rendering of blocks. For example display certain blocks in a horizontal
 * scrolling container or display tables in a specific widget designed for it ([Builder.include]).
 *
 * @see .builder
 * @see .builder
 * @see .create
 * @see .create
 * @see .setMarkdown
 * @see .setParsedMarkdown
 * @see .setParsedMarkdown
 * @since 3.0.0
 */
abstract class MarkwonAdapter : RecyclerView.Adapter<MarkwonAdapter.Holder>() {
    /**
     * Builder to create an instance of [MarkwonAdapter]
     *
     * @see .include
     * @see .reducer
     * @see .build
     */
    interface Builder {
        /**
         * Include a custom [Entry] rendering for a Node. Please note that `node` argument
         * must be *exact* type, as internally there is no validation for inheritance. if multiple
         * nodes should be rendered with the same [Entry] they must specify so explicitly.
         * By calling this method for each.
         *
         * @param node  type of the node to register
         * @param entry [Entry] to be used for `node` rendering
         * @return self
         */
        fun <N : Node> include(node: Class<N>, entry: Entry<in N, out Holder>): Builder

        /**
         * Specify how root Node will be *reduced* to a list of nodes. There is a default
         * [MarkwonReducer] that will be used if not provided explicitly (there is no need to
         * register your own unless you require it).
         *
         * @param reducer [MarkwonReducer]
         * @return self
         * @see MarkwonReducer
         */
        fun reducer(reducer: MarkwonReducer): Builder

        /**
         * @return [MarkwonAdapter]
         */
        fun build(): MarkwonAdapter
    }

    /**
     * @see SimpleEntry
     */
    abstract class Entry<N : Node, H : Holder> {
        abstract fun createHolder(inflater: LayoutInflater, parent: ViewGroup): H

        abstract fun bindHolder(markwon: Markwon, holder: H, node: N)

        /**
         * Will be called when new content is available (clear internal cache if any)
         */
        open fun clear() {
        }

        fun id(node: N): Long {
            return node.hashCode().toLong()
        }

        fun onViewRecycled(holder: H) {
        }
    }

    abstract fun setMarkdown(markwon: Markwon, markdown: String)

    abstract fun setParsedMarkdown(markwon: Markwon, document: Node)

    abstract fun setParsedMarkdown(markwon: Markwon, nodes: MutableList<Node>)

    abstract fun getNodeViewType(node: Class<out Node>): Int

    open class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // please note that this method should be called after constructor
        protected fun <V : View> findView(@IdRes id: Int): V {
            return itemView.findViewById<V>(id)
        }

        // please note that this method should be called after constructor
        protected fun <V : View> requireView(@IdRes id: Int): V {
            val v = itemView.findViewById<V>(id)
            if (v == null) {
                val name: String = if (id == 0 || id == View.NO_ID) {
                    id.toString()
                } else {
                    "R.id." + itemView.resources.getResourceName(id)
                }
                throw NullPointerException(
                    String.format(
                        "No view with id(R.id.%s) is found " + "in layout: %s",
                        name,
                        itemView
                    )
                )
            }
            return v
        }
    }

    companion object {
        fun builderTextViewIsRoot(@LayoutRes defaultEntryLayoutResId: Int): Builder {
            return builder(SimpleEntry.createTextViewIsRoot(defaultEntryLayoutResId) as Entry<Node, Holder>)
        }

        /**
         * Factory method to obtain [Builder] instance.
         *
         * @see Builder
         */
        fun builder(
            @LayoutRes defaultEntryLayoutResId: Int,
            @IdRes defaultEntryTextViewResId: Int
        ): Builder {
            val simpleEntry = SimpleEntry.create(defaultEntryLayoutResId, defaultEntryTextViewResId)
            return builder(simpleEntry as Entry<Node, Holder>)
        }

        fun builder(defaultEntry: Entry<Node, Holder>): Builder {
            return MarkwonAdapterImpl.BuilderImpl(defaultEntry)
        }

        fun createTextViewIsRoot(@LayoutRes defaultEntryLayoutResId: Int): MarkwonAdapter {
            return builderTextViewIsRoot(defaultEntryLayoutResId).build()
        }

        /**
         * Factory method to create a [MarkwonAdapter] for evaluation purposes. Resulting
         * adapter will use default layout for all blocks. Default layout has no styling and should
         * be specified explicitly.
         *
         * @see .create
         * @see .builder
         * @see SimpleEntry
         */
        fun create(
            @LayoutRes defaultEntryLayoutResId: Int,
            @IdRes defaultEntryTextViewResId: Int
        ): MarkwonAdapter {
            return builder(defaultEntryLayoutResId, defaultEntryTextViewResId).build()
        }

        /**
         * Factory method to create a [MarkwonAdapter] that uses supplied entry to render all
         * nodes.
         *
         * @param defaultEntry [Entry] to be used for node rendering
         * @see .builder
         */
        fun create(defaultEntry: Entry<out Node, out Holder>): MarkwonAdapter {
            return builder(defaultEntry as Entry<Node, Holder>).build()
        }
    }
}
