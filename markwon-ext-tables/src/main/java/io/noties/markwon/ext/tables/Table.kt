package io.noties.markwon.ext.tables

import android.text.Spanned
import io.noties.markwon.Markwon
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.CustomNode

/**
 * A class to parse `TableBlock` and return a data-structure that is not dependent
 * on commonmark-java table extension. Can be useful when rendering tables require special
 * handling (multiple views, specific table view) for example when used with `markwon-recycler` artifact
 *
 * @see .parse
 * @since 3.0.0
 */
class Table(private val rows: MutableList<Row>) {
    class Row(
        private val isHeader: Boolean,
        private val columns: MutableList<Column>
    ) {
        fun header(): Boolean {
            return isHeader
        }

        fun columns(): MutableList<Column> {
            return columns
        }

        override fun toString(): String {
            return "Row{" +
                    "isHeader=" + isHeader +
                    ", columns=" + columns +
                    '}'
        }
    }

    class Column(private val alignment: Alignment, private val content: Spanned) {
        fun alignment(): Alignment {
            return alignment
        }

        fun content(): Spanned {
            return content
        }

        override fun toString(): String {
            return "Column{" +
                    "alignment=" + alignment +
                    ", content=" + content +
                    '}'
        }
    }

    enum class Alignment {
        LEFT,
        CENTER,
        RIGHT
    }

    fun rows(): MutableList<Row> {
        return rows
    }

    override fun toString(): String {
        return "Table{" +
                "rows=" + rows +
                '}'
    }

    internal class ParseVisitor(private val markwon: Markwon) : AbstractVisitor() {
        private var rows: MutableList<Row>? = null

        private var pendingRow: MutableList<Column>? = null
        private var pendingRowIsHeader = false

        fun rows(): MutableList<Row>? {
            return rows
        }

        override fun visit(customNode: CustomNode?) {
            if (customNode is TableCell) {
                val cell = customNode

                if (pendingRow == null) {
                    pendingRow = ArrayList(2)
                }

                pendingRow!!.add(Column(alignment(cell.alignment), markwon.render(cell)))
                pendingRowIsHeader = cell.isHeader

                return
            }

            if (customNode is TableHead
                || customNode is TableRow
            ) {
                visitChildren(customNode)

                // this can happen, ignore such row
                pendingRow?.let {
                    if (it.isNotEmpty()) {
                        if (rows == null) {
                            rows = ArrayList(2)
                        }

                        rows!!.add(Row(pendingRowIsHeader, it))
                    }
                }

                pendingRow = null
                pendingRowIsHeader = false

                return
            }

            visitChildren(customNode)
        }

        companion object {
            private fun alignment(alignment: TableCell.Alignment): Alignment {
                val out: Alignment = if (TableCell.Alignment.RIGHT == alignment) {
                    Alignment.RIGHT
                } else if (TableCell.Alignment.CENTER == alignment) {
                    Alignment.CENTER
                } else {
                    Alignment.LEFT
                }
                return out
            }
        }
    }

    companion object {
        /**
         * Factory method to obtain an instance of [Table]
         *
         * @param markdown    Markwon
         * @param tableBlock TableBlock to parse
         * @return parsed [Table] or null
         */
        @JvmStatic
        fun parse(markwon: Markwon, tableBlock: TableBlock): Table? {
            val table: Table?

            val visitor = ParseVisitor(markwon)
            tableBlock.accept(visitor)
            val rows = visitor.rows()

            table = if (rows == null) {
                null
            } else {
                Table(rows)
            }

            return table
        }
    }
}
