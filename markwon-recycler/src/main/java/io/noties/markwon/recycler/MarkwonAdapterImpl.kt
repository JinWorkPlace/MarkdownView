package io.noties.markwon.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonReducer
import org.commonmark.node.Node
import java.util.Collections

internal class MarkwonAdapterImpl(
    private val entries: MutableList<Entry<Node, Holder>>,
    private val defaultEntry: Entry<Node, Holder>,
    private val reducer: MarkwonReducer
) : MarkwonAdapter() {
    private var layoutInflater: LayoutInflater? = null

    private var markwon: Markwon? = null
    private var nodes: MutableList<Node>? = null

    init {
        setHasStableIds(true)
    }

    override fun setMarkdown(markwon: Markwon, markdown: String) {
        setParsedMarkdown(markwon, markwon.parse(markdown))
    }

    override fun setParsedMarkdown(markwon: Markwon, document: Node) {
        setParsedMarkdown(markwon, reducer.reduce(document))
    }

    override fun setParsedMarkdown(markwon: Markwon, nodes: MutableList<Node>) {
        // clear all entries before applying

        defaultEntry.clear()

        var i = 0
        val size = entries.size
        while (i < size) {
            entries[i].clear()
            i++
        }

        this.markwon = markwon
        this.nodes = nodes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }

        val entry = getEntry(viewType)

        return entry.createHolder(layoutInflater!!, parent)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val node = nodes!![position]
        val viewType = getNodeViewType(node.javaClass)

        val entry = getEntry(viewType)

        entry.bindHolder(markwon!!, holder, node)
    }

    override fun getItemCount(): Int {
        return if (nodes != null)
            nodes!!.size
        else
            0
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)

        val entry = getEntry(holder.itemViewType)
        entry.onViewRecycled(holder)
    }

    @get:Suppress("unused")
    val items: MutableList<Node>
        get() = if (nodes != null)
            Collections.unmodifiableList(nodes)
        else mutableListOf()

    override fun getItemViewType(position: Int): Int {
        return getNodeViewType(nodes!![position].javaClass)
    }

    override fun getItemId(position: Int): Long {
        val node = nodes!![position]
        val type = getNodeViewType(node.javaClass)
        val entry = getEntry(type)
        return entry.id(node)
    }

    override fun getNodeViewType(node: Class<out Node>): Int {
        // if has registered -> then return it, else 0
        val hash = node.hashCode()

        if (entries.indexOfFirst { it.hashCode() == hash } > -1) {
            return hash
        }
        return 0
    }

    private fun getEntry(viewType: Int): Entry<Node, Holder> {
        return if (viewType == 0)
            defaultEntry
        else
            entries[viewType]
    }

    internal class BuilderImpl(private val defaultEntry: Entry<Node, Holder>) : Builder {
        private val entries = ArrayList<Entry<Node, Holder>>(3)

        private var reducer: MarkwonReducer? = null

        override fun <N : Node> include(node: Class<N>, entry: Entry<in N, out Holder>): Builder {
            entries.add(entry as Entry<Node, Holder>)
            return this
        }

        override fun reducer(reducer: MarkwonReducer): Builder {
            this.reducer = reducer
            return this
        }

        override fun build(): MarkwonAdapter {
            if (reducer == null) {
                reducer = MarkwonReducer.directChildren()
            }

            return MarkwonAdapterImpl(entries, defaultEntry, reducer!!)
        }
    }
}
