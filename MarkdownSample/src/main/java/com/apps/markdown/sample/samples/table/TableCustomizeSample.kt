package com.apps.markdown.sample.samples.table

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.utils.Dip

@MarkwonSampleInfo(
    id = "20200702135621",
    title = "Customize table theme",
    artifacts = [MarkwonArtifact.EXT_TABLES],
    tags = [Tag.THEME]
)
class TableCustomizeSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "| HEADER | HEADER | HEADER |\n" + "|:----:|:----:|:----:|\n" + "|   测试  |   测试   |   测试   |\n" + "|  测试  |   测试   |  测测测12345试测试测试   |\n" + "|   测试  |   测试   |   123445   |\n" + "|   测试  |   测试   |   (650) 555-1212   |\n" + "|   测试  |   测试   |   [link](#)   |\n"

        val themeConfigure: TablePlugin.ThemeConfigure = object : TablePlugin.ThemeConfigure {
            override fun configureTheme(builder: TableTheme.Builder) {
                val dip: Dip = Dip.create(context)
                builder.tableBorderWidth(dip.toPx(2))
                    .tableBorderColor(android.graphics.Color.YELLOW).tableCellPadding(dip.toPx(4))
                    .tableHeaderRowBackgroundColor(
                        io.noties.markwon.utils.ColorUtils.applyAlpha(
                            android.graphics.Color.RED, 80
                        )
                    ).tableEvenRowBackgroundColor(
                        io.noties.markwon.utils.ColorUtils.applyAlpha(
                            android.graphics.Color.GREEN, 80
                        )
                    ).tableOddRowBackgroundColor(
                        io.noties.markwon.utils.ColorUtils.applyAlpha(
                            android.graphics.Color.BLUE, 80
                        )
                    )
            }
        }

        val markwon: Markwon =
            Markwon.builder(context).usePlugin(TablePlugin.create(themeConfigure)).build()

        markwon.setMarkdown(textView, md)
    }
}
