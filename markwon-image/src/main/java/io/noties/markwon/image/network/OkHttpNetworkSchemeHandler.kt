package io.noties.markwon.image.network

import android.net.Uri
import io.noties.markwon.image.ImageItem
import io.noties.markwon.image.SchemeHandler
import io.noties.markwon.image.network.NetworkSchemeHandler.Companion.contentType
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * @since 4.0.0
 */
class OkHttpNetworkSchemeHandler internal constructor(
    private val factory: Call.Factory
) : SchemeHandler() {
    override fun handle(raw: String, uri: Uri): ImageItem {
        val request = Request.Builder().url(raw).tag(raw).build()

        val response: Response
        try {
            response = factory.newCall(request).execute()
        } catch (t: Throwable) {
            throw IllegalStateException("Exception obtaining network resource: $raw", t)
        }

        checkNotNull(response) { "Could not obtain network response: $raw" }

        val body = response.body
        val inputStream = body?.byteStream()

        checkNotNull(inputStream) { "Response does not contain body: $raw" }

        // important to process content-type as it can have encoding specified (which we should remove)
        val contentType = contentType(response.header(HEADER_CONTENT_TYPE))

        return ImageItem.withDecodingNeeded(contentType, inputStream)
    }

    override fun supportedSchemes(): MutableCollection<String> {
        return mutableListOf(
            NetworkSchemeHandler.SCHEME_HTTP, NetworkSchemeHandler.SCHEME_HTTPS
        )
    }

    companion object {
        /**
         * @see .create
         */
        @JvmOverloads
        fun create(client: OkHttpClient = OkHttpClient()): OkHttpNetworkSchemeHandler {
            // explicit cast, otherwise a recursive call
            return create(client as Call.Factory)
        }

        /**
         * @since 4.0.0
         */
        fun create(factory: Call.Factory): OkHttpNetworkSchemeHandler {
            return OkHttpNetworkSchemeHandler(factory)
        }

        private const val HEADER_CONTENT_TYPE = "Content-Type"
    }
}
