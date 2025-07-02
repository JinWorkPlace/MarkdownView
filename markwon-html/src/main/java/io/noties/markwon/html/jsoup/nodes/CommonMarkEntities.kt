package io.noties.markwon.html.jsoup.nodes

import org.commonmark.internal.util.Html5Entities

object CommonMarkEntities {
    @JvmStatic
    fun isNamedEntity(name: String): Boolean {
        return COMMONMARK_NAMED_ENTITIES!!.containsKey(name)
    }

    @JvmStatic
    fun codepointsForName(name: String, codepoints: IntArray): Int {
        val value = COMMONMARK_NAMED_ENTITIES!![name]
        if (value != null) {
            val length = value.length
            if (length == 1) {
                codepoints[0] = value[0].code
            } else {
                codepoints[0] = value[0].code
                codepoints[1] = value[1].code
            }
            return length
        }
        return 0
    }

    private val COMMONMARK_NAMED_ENTITIES: MutableMap<String, String>?

    init {
        var map: MutableMap<String, String>?
        try {
            val field = Html5Entities::class.java.getDeclaredField("NAMED_CHARACTER_REFERENCES")
            field.isAccessible = true
            map = field.get(null) as MutableMap<String, String>?
        } catch (t: Throwable) {
            map = mutableMapOf<String, String>()
            t.printStackTrace()
        }
        COMMONMARK_NAMED_ENTITIES = map
    }
}
