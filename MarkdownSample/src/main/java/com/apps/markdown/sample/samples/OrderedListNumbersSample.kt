package com.apps.markdown.sample.samples

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon

@MarkwonSampleInfo(
    id = "20201203221806",
    title = "Ordered list numbers",
    artifacts = [MarkwonArtifact.CORE],
    tags = [Tag.RENDERING]
)
class OrderedListNumbersSample : MarkwonTextViewSample() {
    public override fun render() {
        val md =
            "# Ordered lists\n\n" + "1. hello there\n" + "1. hello there and much much more, this text just goes and goes, and should it stop, we' know it\n" + "1. okay, np\n" + "1. hello there\n" + "1. hello there and much much more, this text just goes and goes, and should it stop, we' know it\n" + "1. okay, np\n" + "1. hello there\n" + "1. hello there and much much more, this text just goes and goes, and should it stop, we' know it\n" + "1. okay, np\n" + "1. hello there\n" + "1. hello there and much much more, this text just goes and goes, and should it stop, we' know it\n" + "1. okay, np\n" + "1. hello there\n" + "1. hello there and much much more, this text just goes and goes, and should it stop, we' know it\n" + "1. okay, np\n" + "1. hello there\n" + "1. hello there and much much more, this text just goes and goes, and should it stop, we' know it\n" + "1. okay, np\n" + "1. hello there\n" + "1. hello there and much much more, this text just goes and goes, and should it stop, we' know it\n" + "1. okay, np\n" + "1. hello there\n" + "1. hello there and much much more, this text just goes and goes, and should it stop, we' know it\n" + "1. okay, np\n" + ""

        val markwon: Markwon = Markwon.create(context)
        markwon.setMarkdown(textView, md)
    }
}
