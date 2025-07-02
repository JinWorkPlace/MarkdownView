package io.noties.markwon.recycler.table

import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.utils.ColorUtils

/**
 * Mimics TableTheme to allow uniform table customization
 *
 * @see .create
 * @see TableEntryPlugin
 *
 * @since 3.0.0
 */
open class TableEntryTheme protected constructor(builder: Builder) : TableTheme(builder) {
    @Px
    override fun tableCellPadding(): Int {
        return tableCellPadding
    }

    @ColorInt
    fun tableBorderColor(paint: Paint): Int {
        return if (tableBorderColor == 0)
            ColorUtils.applyAlpha(paint.color, TABLE_BORDER_DEF_ALPHA)
        else
            tableBorderColor
    }

    @Px
    override fun tableBorderWidth(paint: Paint): Int {
        return if (tableBorderWidth < 0) (paint.strokeWidth + .5f).toInt() else
            tableBorderWidth
    }

    @ColorInt
    fun tableOddRowBackgroundColor(paint: Paint): Int {
        return if (tableOddRowBackgroundColor == 0)
            ColorUtils.applyAlpha(paint.color, TABLE_ODD_ROW_DEF_ALPHA)
        else
            tableOddRowBackgroundColor
    }

    @ColorInt
    fun tableEvenRowBackgroundColor(): Int {
        return tableEvenRowBackgroundColor
    }

    @ColorInt
    fun tableHeaderRowBackgroundColor(): Int {
        return tableHeaderRowBackgroundColor
    }

    companion object {
        fun create(tableTheme: TableTheme): TableEntryTheme {
            return TableEntryTheme(tableTheme.asBuilder())
        }
    }
}
