package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon

@MarkwonSampleInfo(
    id = "20200826122247",
    title = "Deeplinks",
    description = "Handling of deeplinks (app handles https scheme to deep link into content)",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.LINKS]
)
class DeeplinksSample : MarkwonTextViewSample() {
    public override fun render() {
        val md =
            "" + "# Deeplinks\n\n" + "The [link](https://noties.io/Markwon/app/sample/20200826122247) to self"

        // nothing special is required
        val markwon: Markwon = Markwon.builder(context).build()

        markwon.setMarkdown(textView, md)
    }
}
