package io.noties.markwon.image.destination

import java.net.MalformedURLException
import java.net.URL

/**
 * @since 4.4.0
 */
class ImageDestinationProcessorRelativeToAbsolute : ImageDestinationProcessor {
    private val base: URL?

    constructor(base: String) {
        this.base = obtain(base)
    }

    constructor(base: URL) {
        this.base = base
    }

    override fun process(destination: String): String {
        var out = destination

        if (base != null) {
            try {
                val u = URL(base, destination)
                out = u.toString()
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
        }
        return out
    }

    companion object {
        fun create(base: String): ImageDestinationProcessorRelativeToAbsolute {
            return ImageDestinationProcessorRelativeToAbsolute(base)
        }

        fun create(base: URL): ImageDestinationProcessorRelativeToAbsolute {
            return ImageDestinationProcessorRelativeToAbsolute(base)
        }

        private fun obtain(base: String?): URL? {
            try {
                return URL(base)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                return null
            }
        }
    }
}
