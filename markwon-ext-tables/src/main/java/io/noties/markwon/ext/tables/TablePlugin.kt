package io.noties.markwon.ext.tables

import android.content.Context
import android.text.Spanned
import android.widget.TextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.tables.TableRowsScheduler.schedule
import io.noties.markwon.ext.tables.TableRowsScheduler.unschedule
import org.commonmark.Extension
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.Node
import org.commonmark.parser.Parser

/**
 * @since 3.0.0
 */
class TablePlugin internal constructor(private val theme: TableTheme) : AbstractMarkwonPlugin() {
    interface ThemeConfigure {
        fun configureTheme(builder: TableTheme.Builder)
    }

    private val visitor: TableVisitor = TableVisitor(theme)

    fun theme(): TableTheme {
        return theme
    }

    override fun configureParser(builder: Parser.Builder) {
        builder.extensions(mutableSetOf<Extension?>(TablesExtension.create()))
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        visitor.configure(builder)
    }

    override fun beforeRender(node: Node) {
        // clear before rendering (as visitor has some internal mutable state)
        visitor.clear()
    }

    override fun beforeSetText(textView: TextView, markdown: Spanned) {
        unschedule(textView)
    }

    override fun afterSetText(textView: TextView) {
        schedule(textView)
    }

    private class TableVisitor(private val tableTheme: TableTheme) {
        private var pendingTableRow: MutableList<TableRowSpan.Cell>? = null
        private var tableRowIsHeader = false
        private var tableRows = 0

        fun clear() {
            pendingTableRow = null
            tableRowIsHeader = false
            tableRows = 0
        }

        fun configure(builder: MarkwonVisitor.Builder) {
            builder // @since 4.1.1 we use TableBlock instead of TableBody to add new lines
                .on(
                    TableBlock::class.java,
                    MarkwonVisitor.NodeVisitor { visitor, tableBlock ->
                        visitor.blockStart(tableBlock)

                        val length = visitor.length()

                        visitor.visitChildren(tableBlock)

                        // @since 4.3.1 apply table span for the full table
                        visitor.setSpans(length, TableSpan())

                        visitor.blockEnd(tableBlock)
                    }).on(
                    TableBody::class.java,
                    MarkwonVisitor.NodeVisitor { visitor, tableBody ->
                        visitor.visitChildren(tableBody)
                        tableRows = 0
                    }).on(
                    TableRow::class.java,
                    MarkwonVisitor.NodeVisitor { visitor, tableRow -> visitRow(visitor, tableRow) })
                .on(
                    TableHead::class.java,
                    MarkwonVisitor.NodeVisitor { visitor, tableHead ->
                        visitRow(
                            visitor,
                            tableHead
                        )
                    }).on(
                    TableCell::class.java, MarkwonVisitor.NodeVisitor { visitor, tableCell ->
                        val length = visitor.length()

                        visitor.visitChildren(tableCell)

                        if (pendingTableRow == null) {
                            pendingTableRow = ArrayList(2)
                        }

                        pendingTableRow!!.add(
                            TableRowSpan.Cell(
                                tableCellAlignment(tableCell.alignment),
                                visitor.builder().removeFromEnd(length)
                            )
                        )

                        tableRowIsHeader = tableCell.isHeader
                    })
        }

        fun visitRow(visitor: MarkwonVisitor, node: Node) {
            val length = visitor.length()

            visitor.visitChildren(node)

            if (pendingTableRow != null) {
                val builder = visitor.builder()

                // @since 2.0.0
                // we cannot rely on hasNext(TableHead) as it's not reliable
                // we must apply new line manually and then exclude it from tableRow span
                val addNewLine: Boolean
                run {
                    val builderLength = builder.length
                    addNewLine = builderLength > 0 && '\n' != builder[builderLength - 1]
                }

                if (addNewLine) {
                    visitor.forceNewLine()
                }

                // @since 1.0.4 Replace table char with non-breakable space
                // we need this because if table is at the end of the text, then it will be
                // trimmed from the final result
                builder.append('\u00a0')

                val span: Any = TableRowSpan(
                    tableTheme, pendingTableRow!!, tableRowIsHeader, tableRows % 2 == 1
                )

                tableRows = if (tableRowIsHeader) 0
                else tableRows + 1

                visitor.setSpans(if (addNewLine) length + 1 else length, span)

                pendingTableRow = null
            }
        }

        companion object {
            @TableRowSpan.Alignment
            private fun tableCellAlignment(alignment: TableCell.Alignment?): Int {
                val out: Int = if (alignment != null) {
                    when (alignment) {
                        TableCell.Alignment.CENTER -> TableRowSpan.ALIGN_CENTER
                        TableCell.Alignment.RIGHT -> TableRowSpan.ALIGN_RIGHT
                        else -> TableRowSpan.ALIGN_LEFT
                    }
                } else {
                    TableRowSpan.ALIGN_LEFT
                }
                return out
            }
        }
    }

    companion object {
        /**
         * Factory method to create a [TablePlugin] with default [TableTheme] instance
         * (obtained via [TableTheme.create] method)
         *
         * @see .create
         * @see .create
         */
        fun create(context: Context): TablePlugin {
            return TablePlugin(TableTheme.create(context))
        }

        fun create(tableTheme: TableTheme): TablePlugin {
            return TablePlugin(tableTheme)
        }

        fun create(themeConfigure: ThemeConfigure): TablePlugin {
            val builder = TableTheme.Builder()
            themeConfigure.configureTheme(builder)
            return TablePlugin(builder.build())
        }
    }
}
