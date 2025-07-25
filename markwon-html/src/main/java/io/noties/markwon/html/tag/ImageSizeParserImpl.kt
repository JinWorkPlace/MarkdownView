package io.noties.markwon.html.tag

import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import io.noties.markwon.html.CssInlineStyleParser
import io.noties.markwon.html.tag.ImageHandler.ImageSizeParser
import io.noties.markwon.image.ImageSize

internal class ImageSizeParserImpl(
    private val inlineStyleParser: CssInlineStyleParser
) : ImageSizeParser {
    override fun parse(attributes: MutableMap<String, String>): ImageSize? {
        // strictly speaking percents when specified directly on an attribute
        // are not part of the HTML spec (I couldn't find any reference)

        var width: ImageSize.Dimension? = null
        var height: ImageSize.Dimension? = null

        // okay, let's first check styles
        val style = attributes["style"]

        if (!TextUtils.isEmpty(style)) {
            var key: String?

            for (cssProperty in inlineStyleParser.parse(style!!)) {
                key = cssProperty.key()

                if ("width" == key) {
                    width = dimension(cssProperty.value())
                } else if ("height" == key) {
                    height = dimension(cssProperty.value())
                }

                if (width != null && height != null) {
                    break
                }
            }
        }

        if (width != null && height != null) {
            return ImageSize(width, height)
        }

        // check tag attributes
        if (width == null) {
            width = dimension(attributes["width"])
        }

        if (height == null) {
            height = dimension(attributes["height"])
        }

        if (width == null && height == null) {
            return null
        }

        return ImageSize(width!!, height!!)
    }

    @VisibleForTesting
    fun dimension(value: String?): ImageSize.Dimension? {
        if (TextUtils.isEmpty(value)) {
            return null
        }

        val length = value!!.length

        for (i in length - 1 downTo -1 + 1) {
            if (Character.isDigit(value[i])) {
                try {
                    val `val` = value.substring(0, i + 1).toFloat()
                    val unit: String? = if (i == length - 1) {
                        null
                    } else {
                        value.substring(i + 1, length)
                    }
                    return ImageSize.Dimension(`val`, unit)
                } catch (_: NumberFormatException) {
                    // value cannot not be represented as a float
                    return null
                }
            }
        }

        return null
    }
}
