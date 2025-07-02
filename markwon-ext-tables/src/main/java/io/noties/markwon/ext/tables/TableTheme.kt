package io.noties.markwon.ext.tables

import android.content.Context
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.noties.markwon.utils.ColorUtils
import io.noties.markwon.utils.Dip

open class TableTheme protected constructor(builder: Builder) {
    // by default 0
    @JvmField
    protected val tableCellPadding: Int

    // by default paint.color * TABLE_BORDER_DEF_ALPHA
    @JvmField
    protected val tableBorderColor: Int

    @JvmField
    protected val tableBorderWidth: Int

    // by default paint.color * TABLE_ODD_ROW_DEF_ALPHA
    @JvmField
    protected val tableOddRowBackgroundColor: Int

    // @since 1.1.1
    // by default no background
    @JvmField
    protected val tableEvenRowBackgroundColor: Int

    // @since 1.1.1
    // by default no background
    @JvmField
    protected val tableHeaderRowBackgroundColor: Int

    init {
        this.tableCellPadding = builder.tableCellPadding
        this.tableBorderColor = builder.tableBorderColor
        this.tableBorderWidth = builder.tableBorderWidth
        this.tableOddRowBackgroundColor = builder.tableOddRowBackgroundColor
        this.tableEvenRowBackgroundColor = builder.tableEvenRowBackgroundColor
        this.tableHeaderRowBackgroundColor = builder.tableHeaderRowBackgroundColor
    }

    /**
     * @since 3.0.0
     */
    fun asBuilder(): Builder {
        return Builder()
            .tableCellPadding(tableCellPadding)
            .tableBorderColor(tableBorderColor)
            .tableBorderWidth(tableBorderWidth)
            .tableOddRowBackgroundColor(tableOddRowBackgroundColor)
            .tableEvenRowBackgroundColor(tableEvenRowBackgroundColor)
            .tableHeaderRowBackgroundColor(tableHeaderRowBackgroundColor)
    }

    open fun tableCellPadding(): Int {
        return tableCellPadding
    }

    open fun tableBorderWidth(paint: Paint): Int {
        val out: Int = if (tableBorderWidth == -1) {
            (paint.strokeWidth + .5f).toInt()
        } else {
            tableBorderWidth
        }
        return out
    }

    fun applyTableBorderStyle(paint: Paint) {
        val color: Int = if (tableBorderColor == 0) {
            ColorUtils.applyAlpha(paint.color, TABLE_BORDER_DEF_ALPHA)
        } else {
            tableBorderColor
        }

        paint.setColor(color)
        // @since 4.3.1 before it was STROKE... change to FILL as we draw border differently
        paint.style = Paint.Style.FILL
    }

    fun applyTableOddRowStyle(paint: Paint) {
        val color: Int = if (tableOddRowBackgroundColor == 0) {
            ColorUtils.applyAlpha(paint.color, TABLE_ODD_ROW_DEF_ALPHA)
        } else {
            tableOddRowBackgroundColor
        }
        paint.setColor(color)
        paint.style = Paint.Style.FILL
    }

    /**
     * @since 1.1.1
     */
    fun applyTableEvenRowStyle(paint: Paint) {
        // by default to background to even row
        paint.setColor(tableEvenRowBackgroundColor)
        paint.style = Paint.Style.FILL
    }

    /**
     * @since 1.1.1
     */
    fun applyTableHeaderRowStyle(paint: Paint) {
        paint.setColor(tableHeaderRowBackgroundColor)
        paint.style = Paint.Style.FILL
    }

    class Builder {
        var tableCellPadding = 0
        var tableBorderColor = 0
        var tableBorderWidth = -1
        var tableOddRowBackgroundColor = 0
        var tableEvenRowBackgroundColor = 0 // @since 1.1.1
        var tableHeaderRowBackgroundColor = 0 // @since 1.1.1

        fun tableCellPadding(@Px tableCellPadding: Int): Builder {
            this.tableCellPadding = tableCellPadding
            return this
        }

        fun tableBorderColor(@ColorInt tableBorderColor: Int): Builder {
            this.tableBorderColor = tableBorderColor
            return this
        }

        fun tableBorderWidth(@Px tableBorderWidth: Int): Builder {
            this.tableBorderWidth = tableBorderWidth
            return this
        }

        fun tableOddRowBackgroundColor(@ColorInt tableOddRowBackgroundColor: Int): Builder {
            this.tableOddRowBackgroundColor = tableOddRowBackgroundColor
            return this
        }

        fun tableEvenRowBackgroundColor(@ColorInt tableEvenRowBackgroundColor: Int): Builder {
            this.tableEvenRowBackgroundColor = tableEvenRowBackgroundColor
            return this
        }

        fun tableHeaderRowBackgroundColor(@ColorInt tableHeaderRowBackgroundColor: Int): Builder {
            this.tableHeaderRowBackgroundColor = tableHeaderRowBackgroundColor
            return this
        }

        fun build(): TableTheme {
            return TableTheme(this)
        }
    }

    companion object {
        @JvmStatic
        fun create(context: Context): TableTheme {
            return buildWithDefaults(context).build()
        }

        fun buildWithDefaults(context: Context): Builder {
            val dip = Dip.create(context)
            return emptyBuilder()
                .tableCellPadding(dip.toPx(4))
                .tableBorderWidth(dip.toPx(1))
        }

        fun emptyBuilder(): Builder {
            return Builder()
        }


        protected const val TABLE_BORDER_DEF_ALPHA: Int = 75

        protected const val TABLE_ODD_ROW_DEF_ALPHA: Int = 22
    }
}
