package com.apps.markdown.sample.samples

import android.content.Context
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import java.util.Collections
import java.util.WeakHashMap

@MarkwonSampleInfo(
    id = "20200707102458",
    title = "Cache Markwon instance",
    description = "A static cache for `Markwon` instance " + "to be associated with a `Context`",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.CACHE]
)
class CacheMarkwonSample : MarkwonTextViewSample() {
    override fun render() {
        render("# First!")
        render("## Second!!")
        render("### Third!!!")
    }

    fun render(md: String) {
        val markwon = MarkwonCache.with(context)
        markwon.setMarkdown(textView, md)
    }
}

object MarkwonCache {
    private val cache = Collections.synchronizedMap(WeakHashMap<Context, Markwon>())

    fun with(context: Context): Markwon {
        return cache[context] ?: {
            Markwon.builder(context).usePlugin(StrikethroughPlugin.create()).build().also {
                cache[context] = it
            }
        }.invoke()
    }
}