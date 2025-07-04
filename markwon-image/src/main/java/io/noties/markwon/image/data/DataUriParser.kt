package io.noties.markwon.image.data

abstract class DataUriParser {
    abstract fun parse(input: String): DataUri?

    internal class Impl : DataUriParser() {
        override fun parse(input: String): DataUri? {
            val index = input.indexOf(',')
            // we expect exactly one comma
            if (index < 0) {
                return null
            }

            val contentType: String?
            val base64: Boolean

            if (index > 0) {
                val part = input.substring(0, index)
                val parts = part.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val length = parts.size
                if (length > 0) {
                    // if one: either content-type or base64
                    if (length == 1) {
                        val value = parts[0]
                        if ("base64" == value) {
                            contentType = null
                            base64 = true
                        } else {
                            contentType = if (value.indexOf('/') > -1) value
                            else null
                            base64 = false
                        }
                    } else {
                        contentType = if (parts[0].indexOf('/') > -1) parts[0]
                        else null
                        base64 = "base64" == parts[length - 1]
                    }
                } else {
                    contentType = null
                    base64 = false
                }
            } else {
                contentType = null
                base64 = false
            }

            val data: String?
            if (index < input.length) {
                val value = input.substring(index + 1, input.length).replace("\n".toRegex(), "")
                data = value.ifEmpty {
                    null
                }
            } else {
                data = null
            }

            return DataUri(contentType, base64, data)
        }
    }

    companion object {
        @JvmStatic
        fun create(): DataUriParser {
            return Impl()
        }
    }
}
