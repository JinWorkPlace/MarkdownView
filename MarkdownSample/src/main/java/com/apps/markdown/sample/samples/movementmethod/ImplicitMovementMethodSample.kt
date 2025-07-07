package com.apps.markdown.sample.samples.movementmethod

import com.apps.markdown.sample.BuildConfig
import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon

@MarkwonSampleInfo(
    id = "20200627075524",
    title = "Implicit movement method",
    description = "By default movement method is applied for links to be clickable",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.MOVEMENT_METHOD, Tag.LINKS, Tag.DEFAULTS]
)
class ImplicitMovementMethodSample : MarkwonTextViewSample() {
    override fun render() {
        val md = """
      # Implicit movement method
      By default `Markwon` applies `LinkMovementMethod` if it is missing,
      so in order for [links](${BuildConfig.GIT_REPOSITORY}) to be clickable
      nothing special should be done
    """.trimIndent()

        // by default Markwon will apply a `LinkMovementMethod` if
        //  it is missing. So, in order for links to be clickable
        //  nothing should be done

        val markwon = Markwon.create(context)

        markwon.setMarkdown(textView, md)
    }
}