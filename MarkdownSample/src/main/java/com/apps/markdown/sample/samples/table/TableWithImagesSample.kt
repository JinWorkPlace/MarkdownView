package com.apps.markdown.sample.samples.table

import com.apps.markdown.sample.annotations.MarkwonArtifact
import com.apps.markdown.sample.annotations.MarkwonSampleInfo
import com.apps.markdown.sample.annotations.Tag
import com.apps.markdown.sample.sample.ui.MarkwonTextViewSample
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.image.ImagesPlugin

@MarkwonSampleInfo(
    id = "20200702135932",
    title = "Images inside table",
    description = "Usage of images inside markdown tables",
    artifacts = [MarkwonArtifact.EXT_TABLES, MarkwonArtifact.IMAGE],
    tags = [Tag.IMAGE]
)
class TableWithImagesSample : MarkwonTextViewSample() {
    override fun render() {
        val md =
            "" + "| HEADER | HEADER |\n" + "|:----:|:----:|\n" + "| ![Build](https://github.com/noties/Markwon/workflows/Build/badge.svg) | Build |\n" + "| Stable | ![stable](https://img.shields.io/maven-central/v/io.noties.markwon/core.svg?label=stable) |\n" + "| BIG | ![image](https://images.pexels.com/photos/41171/brussels-sprouts-sprouts-cabbage-grocery-41171.jpeg) |\n" + "\n"

        val markwon: Markwon = Markwon.builder(context).usePlugin(ImagesPlugin.create())
            .usePlugin(TablePlugin.create(context)).build()

        markwon.setMarkdown(textView, md)
    }
}
