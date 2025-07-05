package com.apps.markdown.sample.readme

import android.view.View
import com.apps.markdown.sample.utils.ReadMeUtils
import io.noties.markwon.LinkResolverDef

class ReadMeLinkResolver : LinkResolverDef() {

    override fun resolve(view: View, link: String) {
        val info = ReadMeUtils.parseRepository(link)
        val url = if (info != null) {
            ReadMeUtils.buildRepositoryReadMeUrl(info.first, info.second)
        } else {
            link
        }
        super.resolve(view, url)
    }
}