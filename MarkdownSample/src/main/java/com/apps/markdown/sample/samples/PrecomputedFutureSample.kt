package com.apps.markdown.sample.samples

import android.util.Log
import com.apps.markdown.sample.R
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.readme.GithubImageDestinationProcessor
import com.apps.markdown.sample.sample.ui.MarkwonRecyclerViewSample
import com.apps.markdown.sample.utils.loadReadMe
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.PrecomputedFutureTextSetterCompat
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.recycler.MarkwonAdapter


private const val TAG = "PrecomputedFutureSample"

@MarkwonSampleInfo(
    id = "20200702092446",
    title = "PrecomputedFutureTextSetterCompat",
    description = "Usage of `PrecomputedFutureTextSetterCompat` " + "inside a `RecyclerView` with appcompat",
    artifacts = [MarkwonArtifact.RECYCLER],
    tags = [Tag.RECYCLE_VIEW, Tag.PRECOMPUTED_TEXT]
)
class PrecomputedFutureSample : MarkwonRecyclerViewSample() {
    override fun render() {
        if (!hasAppCompat()) {
            return
        }

        val md: String = loadReadMe(context)

        val markwon: Markwon =
            Markwon.builder(context).textSetter(PrecomputedFutureTextSetterCompat.create())
                .usePlugin(ImagesPlugin.create()).usePlugin(TablePlugin.create(context))
                .usePlugin(TaskListPlugin.create(context)).usePlugin(StrikethroughPlugin.create())
                .usePlugin(object : AbstractMarkwonPlugin() {
                    override fun configureConfiguration(builder: io.noties.markwon.MarkwonConfiguration.Builder) {
                        builder.imageDestinationProcessor(GithubImageDestinationProcessor())
                    }
                }).build()

        val adapter: MarkwonAdapter =
            MarkwonAdapter.createTextViewIsRoot(R.layout.adapter_appcompat_default_entry)

        recyclerView.setLayoutManager(androidx.recyclerview.widget.LinearLayoutManager(context))
        recyclerView.setAdapter(adapter)

        adapter.setMarkdown(markwon, md)
        adapter.notifyDataSetChanged()
    }

    companion object {
        private fun hasAppCompat(): Boolean {
            try {
                Class.forName("androidx.appcompat.widget.AppCompatTextView")
                return true
            } catch (t: Throwable) {
                Log.e(TAG, "hasAppCompat: ${t.message}", t)
                return false
            }
        }
    }
}
