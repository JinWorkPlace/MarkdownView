package com.apps.markdown.sample.readme

import android.net.Uri
import com.apps.markdown.sample.utils.ReadMeUtils
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration

class ReadMeImageDestinationPlugin(private val data: Uri?) : AbstractMarkwonPlugin() {
    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
        val info = ReadMeUtils.parseInfo(data)
        if (info == null) {
            builder.imageDestinationProcessor(GithubImageDestinationProcessor())
        } else {
            builder.imageDestinationProcessor(
                GithubImageDestinationProcessor(
                    username = info.username, repository = info.repository, branch = info.branch
                )
            )
        }
    }
}