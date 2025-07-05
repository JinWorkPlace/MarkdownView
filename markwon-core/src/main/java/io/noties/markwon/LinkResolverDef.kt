package io.noties.markwon

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Browser
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.net.toUri

open class LinkResolverDef : LinkResolver {
    override fun resolve(view: View, link: String) {
        val uri: Uri = parseLink(link)
        val context = view.context
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w("LinkResolverDef", "Activity was not found for the link: '$link'")
            Log.e("LinkResolverDef", e.message, e)
        }
    }

    companion object {
        // @since 4.3.0
        private const val DEFAULT_SCHEME = "https"

        /**
         * @since 4.3.0
         */
        private fun parseLink(link: String): Uri {
            val uri = link.toUri()
            if (TextUtils.isEmpty(uri.scheme)) {
                return uri.buildUpon().scheme(DEFAULT_SCHEME).build()
            }
            return uri
        }
    }
}
