package io.noties.markwon.recycler.table

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import androidx.core.view.size
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.Table
import io.noties.markwon.ext.tables.Table.Companion.parse
import io.noties.markwon.recycler.MarkwonAdapter
import io.noties.markwon.utils.NoCopySpannableFactory
import org.commonmark.ext.gfm.tables.TableBlock

/**
 * @since 3.0.0
 */
class TableEntry internal constructor(
    @param:LayoutRes private val tableLayoutResId: Int,
    @param:IdRes private val tableIdRes: Int,
    @param:LayoutRes private val textLayoutResId: Int,
    @param:IdRes private val textIdRes: Int,
    private val isRecyclable: Boolean, // by default true
    private val cellTextCenterVertical: Boolean
) : MarkwonAdapter.Entry<TableBlock, TableEntry.Holder>() {
    interface Builder {
        /**
         * @param tableLayoutResId layout with TableLayout
         * @param tableIdRes       id of the TableLayout inside specified layout
         * @see .tableLayoutIsRoot
         */
        fun tableLayout(@LayoutRes tableLayoutResId: Int, @IdRes tableIdRes: Int): Builder

        /**
         * @param tableLayoutResId layout with TableLayout as the root view
         * @see .tableLayout
         */
        fun tableLayoutIsRoot(@LayoutRes tableLayoutResId: Int): Builder

        /**
         * @param textLayoutResId layout with TextView
         * @param textIdRes       id of the TextView inside specified layout
         * @see .textLayoutIsRoot
         */
        fun textLayout(@LayoutRes textLayoutResId: Int, @IdRes textIdRes: Int): Builder

        /**
         * @param textLayoutResId layout with TextView as the root view
         * @see .textLayout
         */
        fun textLayoutIsRoot(@LayoutRes textLayoutResId: Int): Builder

        /**
         * @param cellTextCenterVertical if text inside a table cell should centered
         * vertically (by default `true`)
         */
        fun cellTextCenterVertical(cellTextCenterVertical: Boolean): Builder

        /**
         * @param isRecyclable flag to set on RecyclerView.ViewHolder (by default `true`)
         */
        fun isRecyclable(isRecyclable: Boolean): Builder

        fun build(): TableEntry
    }

    interface BuilderConfigure {
        fun configure(builder: Builder)
    }

    private var inflater: LayoutInflater? = null

    private val map: MutableMap<TableBlock, Table> = HashMap(3)

    override fun createHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(isRecyclable, tableIdRes, inflater.inflate(tableLayoutResId, parent, false))
    }

    override fun bindHolder(markwon: Markwon, holder: Holder, node: TableBlock) {
        var table = map[node]
        if (table == null) {
            table = parse(markwon, node)
            map.put(node, table!!)
        }

        // check if this exact TableBlock was already applied
        // set tag of tableLayoutResId as it's 100% to be present (we still allow 0 as
        // tableIdRes if tableLayoutResId has TableLayout as root view)
        val layout = holder.tableLayout
        if (table == layout.getTag(tableLayoutResId)) {
            return
        }

        // set this flag to indicate what table instance we current display
        layout.setTag(tableLayoutResId, table)

        val plugin = markwon.getPlugin(TableEntryPlugin::class.java)
        checkNotNull(plugin) { "No TableEntryPlugin is found. Make sure that it " + "is _used_ whilst configuring Markdown instance" }

        // we must remove unwanted ones (rows and columns)
        val theme = plugin.theme()
        val borderWidth: Int
        val borderColor: Int
        val cellPadding: Int
        run {
            val textView = ensureTextView(layout, 0, 0)
            borderWidth = theme.tableBorderWidth(textView.paint)
            borderColor = theme.tableBorderColor(textView.paint)
            cellPadding = theme.tableCellPadding()
        }

        ensureTableBorderBackground(layout, borderWidth, borderColor)


//        layout.setPadding(borderWidth, borderWidth, borderWidth, borderWidth);
//        layout.setClipToPadding(borderWidth == 0);
        val rows = table.rows()

        val rowsSize = rows.size

        // all rows should have equal number of columns
        val columnsSize = if (rowsSize > 0) rows[0].columns().size else 0

        var row: Table.Row
        var column: Table.Column?

        var tableRow: TableRow?

        for (y in 0..<rowsSize) {
            row = rows[y]
            tableRow = ensureRow(layout, y)

            for (x in 0..<columnsSize) {
                column = row.columns()[x]

                val textView = ensureTextView(layout, y, x)
                textView.setGravity(textGravity(column.alignment(), cellTextCenterVertical))
                textView.paint.isFakeBoldText = row.header()

                // apply padding only if not specified in theme (otherwise just use the value from layout)
                if (cellPadding > 0) {
                    textView.setPadding(cellPadding, cellPadding, cellPadding, cellPadding)
                }

                ensureTableBorderBackground(textView, borderWidth, borderColor)
                markwon.setParsedMarkdown(textView, column.content())
            }

            // row appearance
            if (row.header()) {
                tableRow.setBackgroundColor(theme.tableHeaderRowBackgroundColor())
            } else {
                // as we currently have no support for tables without head
                // we shift even/odd calculation a bit (head should not be included in even/odd calculation)
                val isEven = (y % 2) == 1
                if (isEven) {
                    tableRow.setBackgroundColor(theme.tableEvenRowBackgroundColor())
                } else {
                    // just take first
                    val textView = ensureTextView(layout, y, 0)
                    tableRow.setBackgroundColor(theme.tableOddRowBackgroundColor(textView.paint))
                }
            }
        }

        // clean up here of un-used rows and columns
        removeUnused(layout, rowsSize, columnsSize)
    }

    private fun ensureRow(layout: TableLayout, row: Int): TableRow {
        val count = layout.size

        // fill the requested views until we have added the `row` one
        if (row >= count) {
            val context = layout.context

            var diff = row - count + 1
            while (diff > 0) {
                layout.addView(TableRow(context))
                diff -= 1
            }
        }

        // return requested child (here it always should be the last one)
        return layout.getChildAt(row) as TableRow
    }

    private fun ensureTextView(layout: TableLayout, row: Int, column: Int): TextView {
        val tableRow = ensureRow(layout, row)
        val count = tableRow.size

        if (column >= count) {
            val inflater = ensureInflater(layout.context)

            var textViewChecked = false

            var view: View
            var textView: TextView?
            var layoutParams: ViewGroup.LayoutParams

            var diff = column - count + 1

            while (diff > 0) {
                view = inflater.inflate(textLayoutResId, tableRow, false)

                // we should have `match_parent` as height (important for borders and text-vertical-align)
                layoutParams = view.layoutParams
                if (layoutParams.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                }

                // it will be enough to check only once
                if (!textViewChecked) {
                    if (textIdRes == 0) {
                        if (view !is TextView) {
                            val name =
                                layout.context.resources.getResourceName(textLayoutResId)
                            throw IllegalStateException(
                                String.format(
                                    "textLayoutResId(R.layout.%s) " + "has other than TextView root view. Specify TextView ID explicitly",
                                    name
                                )
                            )
                        }
                        textView = view
                    } else {
                        textView = view.findViewById(textIdRes)
                        if (textView == null) {
                            val r = layout.context.resources
                            val layoutName = r.getResourceName(textLayoutResId)
                            val idName = r.getResourceName(textIdRes)
                            throw NullPointerException(
                                String.format(
                                    "textLayoutResId(R.layout.%s) " + "has no TextView found by id(R.id.%s): %s",
                                    layoutName,
                                    idName,
                                    view
                                )
                            )
                        }
                    }
                    // mark as checked
                    textViewChecked = true
                } else {
                    textView = if (textIdRes == 0) {
                        view as TextView
                    } else {
                        view.findViewById(textIdRes)
                    }
                }

                // we should set SpannableFactory during creation (to avoid another setText method)
                textView.setSpannableFactory(NoCopySpannableFactory.getInstance())
                tableRow.addView(textView)

                diff -= 1
            }
        }

        // we can skip all the validation here as we have validated our views whilst inflating them
        val last = tableRow.getChildAt(column)
        return if (textIdRes == 0) {
            last as TextView
        } else {
            last.findViewById(textIdRes)
        }
    }

    private fun ensureTableBorderBackground(
        view: View,
        @Px borderWidth: Int,
        @ColorInt borderColor: Int
    ) {
        if (borderWidth == 0) {
            view.background = null
        } else {
            val drawable = view.background
            if (drawable !is TableBorderDrawable) {
                val borderDrawable = TableBorderDrawable()
                borderDrawable.update(borderWidth, borderColor)
                view.background = borderDrawable
            } else {
                drawable.update(borderWidth, borderColor)
            }
        }
    }

    private fun ensureInflater(context: Context): LayoutInflater {
        if (inflater == null) {
            inflater = LayoutInflater.from(context)
        }
        return inflater!!
    }

    override fun clear() {
        map.clear()
    }

    class Holder(isRecyclable: Boolean, @IdRes tableLayoutIdRes: Int, itemView: View) :
        MarkwonAdapter.Holder(itemView) {
        val tableLayout: TableLayout

        init {
            // we must call this method only once (it's somehow _paired_ inside, so
            // any call in `onCreateViewHolder` or `onBindViewHolder` will log an error
            // `isRecyclable decremented below 0` which make little sense here)
            setIsRecyclable(isRecyclable)

            val tableLayout: TableLayout
            if (tableLayoutIdRes == 0) {
                // try to cast directly
                check(itemView is TableLayout) { "Root view is not TableLayout. Please provide " + "TableLayout ID explicitly" }
                tableLayout = itemView
            } else {
                tableLayout = requireView<TableLayout?>(tableLayoutIdRes)!!
            }
            this.tableLayout = tableLayout
        }
    }

    internal class BuilderImpl : Builder {
        private var tableLayoutResId = 0
        private var tableIdRes = 0

        private var textLayoutResId = 0
        private var textIdRes = 0

        private var cellTextCenterVertical = true

        private var isRecyclable = true

        override fun tableLayout(tableLayoutResId: Int, tableIdRes: Int): Builder {
            this.tableLayoutResId = tableLayoutResId
            this.tableIdRes = tableIdRes
            return this
        }

        override fun tableLayoutIsRoot(tableLayoutResId: Int): Builder {
            this.tableLayoutResId = tableLayoutResId
            this.tableIdRes = 0
            return this
        }

        override fun textLayout(textLayoutResId: Int, textIdRes: Int): Builder {
            this.textLayoutResId = textLayoutResId
            this.textIdRes = textIdRes
            return this
        }

        override fun textLayoutIsRoot(textLayoutResId: Int): Builder {
            this.textLayoutResId = textLayoutResId
            this.textIdRes = 0
            return this
        }

        override fun cellTextCenterVertical(cellTextCenterVertical: Boolean): Builder {
            this.cellTextCenterVertical = cellTextCenterVertical
            return this
        }

        override fun isRecyclable(isRecyclable: Boolean): Builder {
            this.isRecyclable = isRecyclable
            return this
        }

        override fun build(): TableEntry {
            check(tableLayoutResId != 0) { "`tableLayoutResId` argument is required" }

            check(textLayoutResId != 0) { "`textLayoutResId` argument is required" }

            return TableEntry(
                tableLayoutResId,
                tableIdRes,
                textLayoutResId,
                textIdRes,
                isRecyclable,
                cellTextCenterVertical
            )
        }
    }

    companion object {
        fun builder(): Builder {
            return BuilderImpl()
        }

        fun create(configure: BuilderConfigure): TableEntry {
            val builder: Builder = builder()
            configure.configure(builder)
            return builder.build()
        }

        @VisibleForTesting
        fun removeUnused(layout: TableLayout, usedRows: Int, usedColumns: Int) {
            // clean up rows

            val rowsCount = layout.size
            if (rowsCount > usedRows) {
                layout.removeViews(usedRows, (rowsCount - usedRows))
            }

            // validate columns
            // here we can use usedRows as children count
            var tableRow: TableRow
            var columnCount: Int

            for (i in 0..<usedRows) {
                tableRow = layout.getChildAt(i) as TableRow
                columnCount = tableRow.size
                if (columnCount > usedColumns) {
                    tableRow.removeViews(usedColumns, (columnCount - usedColumns))
                }
            }
        }

        // we will use gravity instead of textAlignment because min sdk is 16 (textAlignment starts at 17)
        @SuppressLint("RtlHardcoded")
        @VisibleForTesting
        fun textGravity(alignment: Table.Alignment, cellTextCenterVertical: Boolean): Int {

            val gravity: Int = when (alignment) {
                Table.Alignment.LEFT -> Gravity.LEFT
                Table.Alignment.CENTER -> Gravity.CENTER_HORIZONTAL
                Table.Alignment.RIGHT -> Gravity.RIGHT
            }

            if (cellTextCenterVertical) {
                return gravity or Gravity.CENTER_VERTICAL
            }

            // do not center vertically
            return gravity
        }
    }
}
