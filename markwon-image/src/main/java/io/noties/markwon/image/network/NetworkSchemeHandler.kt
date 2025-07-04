package io.noties.markwon.image.network

import android.net.Uri
import io.noties.markwon.image.ImageItem
import io.noties.markwon.image.SchemeHandler
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * A simple network scheme handler that is not dependent on any external libraries.
 *
 * @see .create
 * @since 3.0.0
 */
class NetworkSchemeHandler internal constructor() : SchemeHandler() {
    override fun handle(raw: String, uri: Uri): ImageItem {
        val imageItem: ImageItem
        try {
            val url = URL(raw)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            val responseCode = connection.getResponseCode()
            if (responseCode >= 200 && responseCode < 300) {
                val contentType: String? = contentType(connection.getHeaderField("Content-Type"))
                val inputStream: InputStream = BufferedInputStream(connection.getInputStream())
                imageItem = ImageItem.withDecodingNeeded(contentType, inputStream)
            } else {
                throw IOException("Bad response code: $responseCode, url: $raw")
            }
        } catch (e: IOException) {
            throw IllegalStateException("Exception obtaining network resource: $raw", e)
        }

        return imageItem
    }

    override fun supportedSchemes(): MutableCollection<String> {
        return mutableListOf(SCHEME_HTTP, SCHEME_HTTPS)
    }

    companion object {
        const val SCHEME_HTTP: String = "http"
        const val SCHEME_HTTPS: String = "https"

        @JvmStatic
        fun create(): NetworkSchemeHandler {
            return NetworkSchemeHandler()
        }

        @JvmStatic
        fun contentType(contentType: String?): String? {
            if (contentType == null) {
                return null
            }

            val index = contentType.indexOf(';')
            if (index > -1) {
                return contentType.substring(0, index)
            }

            return contentType
        }
    }
}
