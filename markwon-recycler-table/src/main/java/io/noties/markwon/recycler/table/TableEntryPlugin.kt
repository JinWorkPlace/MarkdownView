package io.noties.markwon.recycler.table

import android.content.Context
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TablePlugin.ThemeConfigure
import io.noties.markwon.ext.tables.TableTheme
import org.commonmark.Extension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser

/**
 * This plugin must be used instead of [TablePlugin] when a markdown
 * table is intended to be used in a RecyclerView via [TableEntry]. This is required
 * because TablePlugin additionally processes markdown tables to be displayed in *limited*
 * context of a TextView. If TablePlugin will be used, [TableEntry] will display table,
 * but no content will be present
 *
 * @since 3.0.0
 */
class TableEntryPlugin internal constructor(
    private val theme: TableEntryTheme
) : AbstractMarkwonPlugin() {
    fun theme(): TableEntryTheme {
        return theme
    }

    override fun configureParser(builder: Parser.Builder) {
        builder.extensions(mutableSetOf<Extension?>(TablesExtension.create()))
    }

    companion object {
        fun create(context: Context): TableEntryPlugin {
            val tableTheme = TableTheme.create(context)
            return create(tableTheme)
        }

        fun create(tableTheme: TableTheme): TableEntryPlugin {
            return TableEntryPlugin(TableEntryTheme.create(tableTheme))
        }

        fun create(themeConfigure: ThemeConfigure): TableEntryPlugin {
            val builder = TableTheme.Builder()
            themeConfigure.configureTheme(builder)
            return TableEntryPlugin(TableEntryTheme(builder))
        }

        fun create(plugin: TablePlugin): TableEntryPlugin {
            return create(plugin.theme())
        }
    }
}
