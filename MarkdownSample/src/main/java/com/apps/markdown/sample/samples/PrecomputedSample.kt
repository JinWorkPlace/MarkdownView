package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.PrecomputedTextSetterCompat
import java.util.concurrent.Executors

@MarkwonSampleInfo(
    id = "20200702091654",
    title = "PrecomputedTextSetterCompat",
    description = "`TextSetter` to use `PrecomputedTextSetterCompat`",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.PRECOMPUTED_TEXT]
)
class PrecomputedSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Heading\n" + "**bold** some precomputed spans via `PrecomputedTextSetterCompat`"

        val markwon: Markwon = Markwon.builder(context)
            .textSetter(PrecomputedTextSetterCompat.create(Executors.newCachedThreadPool())).build()

        markwon.setMarkdown(textView, md)
    }
}
