package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon

@MarkwonSampleInfo(
    id = "20200629124005",
    title = "Links without scheme",
    description = "Links without scheme are considered to be `https`",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.LINKS, Tag.DEFAULTS]
)
class LinkWithoutSchemeSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "# Links without scheme\n" + "[a link without scheme](github.com) is considered to be `https`.\n" + "Override `LinkResolverDef` to change this functionality" + ""

        val markwon: Markwon = Markwon.create(context)

        markwon.setMarkdown(textView, md)
    }
}
