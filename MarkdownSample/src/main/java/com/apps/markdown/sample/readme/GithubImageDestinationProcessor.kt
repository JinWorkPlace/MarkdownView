package com.apps.markdown.sample.readme

import android.text.TextUtils
import androidx.core.net.toUri
import io.noties.markwon.image.destination.ImageDestinationProcessor
import io.noties.markwon.image.destination.ImageDestinationProcessorRelativeToAbsolute

class GithubImageDestinationProcessor(
    username: String = "noties", repository: String = "Markwon", branch: String = "master"
) : ImageDestinationProcessor() {

    private val processor =
        ImageDestinationProcessorRelativeToAbsolute("https://github.com/$username/$repository/raw/$branch/")

    override fun process(destination: String): String {
        // process only images without scheme information
        val uri = destination.toUri()
        return if (TextUtils.isEmpty(uri.scheme)) {
            processor.process(destination)
        } else {
            destination
        }
    }
}