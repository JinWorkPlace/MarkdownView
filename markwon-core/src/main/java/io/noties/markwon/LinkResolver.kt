package io.noties.markwon

import android.view.View

/**
 * @see LinkResolverDef
 *
 * @see MarkwonConfiguration.Builder.linkResolver
 * @since 4.0.0
 */
interface LinkResolver {
    fun resolve(view: View, link: String)
}
