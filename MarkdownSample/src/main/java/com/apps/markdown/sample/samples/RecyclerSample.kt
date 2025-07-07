package com.apps.markdown.sample.samples

import com.apps.markdown.sample.R
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.readme.GithubImageDestinationProcessor
import com.apps.markdown.sample.sample.ui.MarkwonRecyclerViewSample
import com.apps.markdown.sample.utils.loadReadMe
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.recycler.MarkwonAdapter
import io.noties.markwon.recycler.table.TableEntry
import io.noties.markwon.recycler.table.TableEntry.BuilderConfigure
import io.noties.markwon.recycler.table.TableEntryPlugin
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.node.FencedCodeBlock

@MarkwonSampleInfo(
    id = "20200702101750",
    title = "RecyclerView",
    description = "Usage with `RecyclerView`",
    artifacts = [MarkwonArtifact.RECYCLER, MarkwonArtifact.RECYCLER_TABLE],
    tags = [Tag.RECYCLE_VIEW]
)
class RecyclerSample : MarkwonRecyclerViewSample() {
    override fun render() {
        val md: String = loadReadMe(context)

        val markwon: Markwon = Markwon.builder(context).usePlugin(ImagesPlugin.create())
            .usePlugin(TableEntryPlugin.create(context)).usePlugin(HtmlPlugin.create())
            .usePlugin(StrikethroughPlugin.create()).usePlugin(TaskListPlugin.create(context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: io.noties.markwon.MarkwonConfiguration.Builder) {
                    builder.imageDestinationProcessor(GithubImageDestinationProcessor())
                }

                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    builder.on(
                        FencedCodeBlock::class.java,
                        MarkwonVisitor.NodeVisitor { visitor: MarkwonVisitor, fencedCodeBlock: FencedCodeBlock ->
                            // we actually won't be applying code spans here, as our custom view will
                            // draw background and apply mono typeface
                            //
                            // NB the `trim` operation on literal (as code will have a new line at the end)
                            val code: CharSequence =
                                visitor.configuration().syntaxHighlight().highlight(
                                    fencedCodeBlock.info,
                                    fencedCodeBlock.literal.trim { it <= ' ' })
                            visitor.builder().append(code)
                        })
                }
            }).build()

        val adapter: MarkwonAdapter =
            MarkwonAdapter.builderTextViewIsRoot(R.layout.adapter_node).include(
                FencedCodeBlock::class.java, io.noties.markwon.recycler.SimpleEntry.create(
                    R.layout.adapter_node_code_block, R.id.text_view
                )
            ).include(TableBlock::class.java, TableEntry.create(object : BuilderConfigure {
                override fun configure(builder: TableEntry.Builder) {
                    builder.tableLayout(R.layout.adapter_node_table_block, R.id.table_layout)
                        .textLayoutIsRoot(R.layout.view_table_entry_cell)
                }

            })).build()

        recyclerView.setLayoutManager(androidx.recyclerview.widget.LinearLayoutManager(context))
        recyclerView.setAdapter(adapter)

        adapter.setMarkdown(markwon, md)
        adapter.notifyDataSetChanged()
    }
}
